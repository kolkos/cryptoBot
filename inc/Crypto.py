"""
File:         Crypto.py

Responsible for handling the Crypto requests and calculations

Class: Crypto

"""

import os
import time
import uuid
import requests
import json
import sys
import ConfigParser
from datetime import datetime
from collections import OrderedDict
from General import General
from Database import Database

class Crypto(object):
    """
    Crypto class
    """
    def __init__(self):
        self.general = General()
        self.database = Database()
        self.request_id = None
        self.coin_request_id = None
        self.requested_by = None

    def set_requested_by(self, name='Scheduler'):
        """
        Setting the name of the requester, by default it is the scheduler. If requested
        by a telegram user, the username will be set.
        """
        self.requested_by = name
        return

    def request_balances(self, coin='all', begin=False):
        """
        This method loops through the keys in the CryptoCurrency section.
        """
        # create a unique ID for the request
        self.request_id = uuid.uuid4()
        
        kwargs = {
            'class': self.__class__.__name__,
            'method': self.request_balances.__name__,
            'request_id': self.request_id,
            'begin': begin,
            'action': "Method called"
        }
        start_time = time.time()
        self.general.logger(3, **kwargs)

        # check if coin is set
        if coin != 'all':
            # check if the coin is in the config file
            try:
                dummy = self.general.config.get('CryptoCurrency', coin)
                self.request_coin_balance(coin)
                status_msg = self.create_status_message(coin, begin)
            except ConfigParser.NoOptionError:
                status_msg = "Sorry ik ken de coin '{}' niet...".format(coin)
                
            
        else:
            # loop through the defined crypto's
            status_msg = ""
            for coin in self.general.config.options('CryptoCurrency'):
                self.request_coin_balance(coin)
                status_msg += self.create_status_message(coin, begin)

        execution_time = time.time() - start_time
        kwargs = {
            'class': self.__class__.__name__,
            'method': self.request_balances.__name__,
            'action': "Method finished",
            'execution_time': execution_time
        }
        self.general.logger(3, **kwargs)

        return status_msg

    def do_api_request(self, url):
        """
        General method to handle api request and do some logging
        """
        kwargs = {
            'class': self.__class__.__name__,
            'method': self.do_api_request.__name__,
            'action': "Method called",
            'request_id': self.request_id,
            'coin_request_id': self.coin_request_id,
            'url': url
        }
        start_time = time.time()
        self.general.logger(3, **kwargs)

        kwargs = {
            'class': self.__class__.__name__,
            'method': self.do_api_request.__name__,
            'action': "Placing API request",
            'request_id': self.request_id,
            'coin_request_id': self.coin_request_id,
            'url': url
        }
        self.general.logger(2, **kwargs)

        try:
            resp = requests.get(url)
            data = resp.json()
        except requests.exceptions.RequestException as e: 
            kwargs = {
                'class': self.__class__.__name__,
                'method': self.do_api_request.__name__,
                'action': "Placing API request",
                'request_id': self.request_id,
                'coin_request_id': self.coin_request_id,
                'error': str(e)
            }
            self.general.logger(1, **kwargs)
            return
        
        kwargs = {
            'class': self.__class__.__name__,
            'method': self.do_api_request.__name__,
            'result': "Request received",
            'request_id': self.request_id,
            'coin_request_id': self.coin_request_id,
            'url': url,
            'status': resp.status_code,
            'data': str(data)
        }
        self.general.logger(2, **kwargs)

        execution_time = time.time() - start_time
        kwargs = {
            'class': self.__class__.__name__,
            'method': self.do_api_request.__name__,
            'request_id': self.request_id,
            'coin_request_id': self.coin_request_id,
            'action': "Method finished",
            'execution_time': execution_time
        }
        self.general.logger(3, **kwargs)

        return data

    def request_coin_balance(self, coin):
        """
        This method does a api request to receive the current balance of a wallet. This method
        uses the CryptoCurrency section of the config file. The name of the coin should match the
        key within this section.
        """
        # create a unique id for the coin request
        self.coin_request_id = uuid.uuid4()

        kwargs = {
            'class': self.__class__.__name__,
            'method': self.request_coin_balance.__name__,
            'action': "Method called",
            'request_id': self.request_id,
            'coin_request_id': self.coin_request_id,
            'coin': coin
        }
        start_time = time.time()
        self.general.logger(3, **kwargs)
        
        url = 'https://api.blockcypher.com/v1/'\
              + coin + '/main/addrs/'\
              + self.general.config.get('CryptoCurrency', coin) + '/balance'
        
        data = self.do_api_request(url)

        balance_sat = float(data['final_balance'])
        balance_coin = balance_sat * 0.00000001

        value = self.calculate_value(coin, balance_coin)

        # now write it to the rapport
        self.write_balance_to_database(coin, balance_sat, balance_coin, value)

        #print str(data)
        print "{} - {} - {} - {}".format(coin, balance_sat, balance_coin, value)

        execution_time = time.time() - start_time
        kwargs = {
            'class': self.__class__.__name__,
            'method': self.request_coin_balance.__name__,
            'request_id': self.request_id,
            'coin_request_id': self.coin_request_id,
            'action': "Method finished",
            'execution_time': execution_time
        }
        self.general.logger(3, **kwargs)

        return

    def calculate_value(self, coin, balance):
        """
        Method to get the current value of the coins. The bitstamp API is used for this
        request
        """
        
        kwargs = {
            'class': self.__class__.__name__,
            'method': self.calculate_value.__name__,
            'action': "Method called",
            'request_id': self.request_id,
            'coin_request_id': self.coin_request_id,
            'coin': coin,
            'balance': balance
        }
        start_time = time.time()
        self.general.logger(3, **kwargs)

        # create the url
        url = 'https://www.bitstamp.net/api/v2/ticker_hour/' + coin + self.general.config.get('General', 'currency') + '/'

        data = self.do_api_request(url)

        #print json.dumps(data, sort_keys=True, indent=4)

        # now calculate the value
        value_last = float(data['last'])

        value = balance * value_last

        value = round(value, 2)

        execution_time = time.time() - start_time
        kwargs = {
            'class': self.__class__.__name__,
            'method': self.calculate_value.__name__,
            'request_id': self.request_id,
            'coin_request_id': self.coin_request_id,
            'action': "Method finished",
            'execution_time': execution_time,
            'value': value
        }
        self.general.logger(3, **kwargs)

        return value

    def write_balance_to_database(self, coin, balance_sat, balance_coin, current_value):
        """
        This methods writes te requested results to the database. This data will be used to create a
        historic overview of the balance(s)
        """
        kwargs = {
            'class': self.__class__.__name__,
            'method': self.write_balance_to_database.__name__,
            'action': "Method called",
            'request_id': self.request_id,
            'coin_request_id': self.coin_request_id,
            'coin': coin,
            'balance_sat': balance_sat,
            'balance_coin': balance_coin,
            'current_value': current_value,
        }
        start_time = time.time()
        self.general.logger(3, **kwargs)

        # build query
        query = "INSERT INTO requests"\
              + "(requestedBy, coin, waardeSatoshi, waardeCoin, huidigeWaarde, currency)"\
              + "VALUES (?, ?, ?, ?, ?, ?)"
        
        values = [(self.requested_by, coin, balance_sat, balance_coin, current_value, self.general.config.get('General', 'currency'))]

        self.database.update_handler(query, values)

        execution_time = time.time() - start_time
        kwargs = {
            'class': self.__class__.__name__,
            'method': self.write_balance_to_database.__name__,
            'request_id': self.request_id,
            'coin_request_id': self.coin_request_id,
            'action': "Method finished",
            'execution_time': execution_time
        }
        self.general.logger(3, **kwargs)

        return
    
    def parse_csv_line(self, line):
        """
        This method splits the line into usable values and returns them into a dict
        """
        result = {}

        # remove the quotes
        line = line.replace('"','')

        # split the line by comma (,)
        line_parts = line.split(',')

        # now write the results into the dictionary
        result['timestamp'] = str(line_parts[0])
        result['requested_by'] = str(line_parts[1])
        result['coin'] = str(line_parts[2])
        result['balance_in_satoshi'] = float(line_parts[3])
        result['balance_in_coins'] = float(line_parts[4])
        result['current_value'] = float(line_parts[5])

        return result

    def calculate_difference_percentage(self, old_value, new_value):
        """
        Method to calculate the difference between the previous value and the new
        value. The result is a percentage (positive or negative).
        """
        percentage = (100 * new_value) / old_value
        percentage = round(percentage, 3)
        percentage = percentage - 100

        percentage_string = "{}%".format(percentage)

        if percentage > 0:
            percentage_string = "+" + percentage_string

        return percentage_string

    def create_status_message(self, coin, begin):
        """
        This method creates the status message for telegram. It will calculate the difference between
        the last two results.
        """

        # first request the second to last row
        if begin:
            query = "SELECT * FROM requests WHERE coin = ? ORDER BY id ASC LIMIT 1"
            
        else:
            query = "SELECT * FROM"\
                + " (SELECT * FROM requests WHERE coin = ? ORDER BY id DESC LIMIT 2) x"\
                + " ORDER BY id LIMIT 1"
        
        values = (coin, )

        results_previous = self.database.select_handler(query, values)

        print "Second to last line:"
        print json.dumps(results_previous, sort_keys=False, indent=6)

        # Now get the last result
        query = "SELECT * FROM requests WHERE coin = ? ORDER BY id DESC LIMIT 1"
        results_now = self.database.select_handler(query, values)

        # now generate the status message
        result_string  = "Coin: *{}*\n".format(coin.upper())
        result_string += "Huidige balans: `{}` ({})\n".format(
            results_now[0][5],
            self.calculate_difference_percentage(results_previous[0][5], results_now[0][5])
        )
        result_string += "Huidige waarde: `{}` euro ({})\n".format(
            results_now[0][6],
            self.calculate_difference_percentage(results_previous[0][6], results_now[0][6])
        )
        result_string += "Vorige keer opgevraagd _{}_ door *{}*\n\n".format(
            results_previous[0][1],
            results_previous[0][2],
        )


        return result_string
    
    def create_status_message_old(self, coin):
        """
        Method to create status messages
        """
        file_name = coin + "_requests_rapport.csv"

        with open(file_name, "r") as fh:
            lines = []
            for line in fh:
                line = line.replace('\n','')
                lines.append(line)

        # check if the number of lines is greater than two, if so we could calculate the difference
        if len(lines) > 2:
            last_line = lines[-1]
            second_to_last_line = lines[-2]

            last_line_values = self.parse_csv_line(last_line)
            second_to_last_line_values = self.parse_csv_line(second_to_last_line)

            result_string  = "Coin: *{}*\n".format(coin.upper())
            result_string += "Huidige balans: `{}` ({})\n".format(
                last_line_values['balance_in_coins'],
                self.calculate_difference_percentage(second_to_last_line_values['balance_in_coins'], last_line_values['balance_in_coins'])
            )
            result_string += "Huidige waarde: `{}` euro ({})\n".format(
                last_line_values['current_value'],
                self.calculate_difference_percentage(second_to_last_line_values['current_value'], last_line_values['current_value'])
            )
            result_string += "Vorige keer opgevraagd _{}_ door *{}*\n\n".format(
                second_to_last_line_values['timestamp'],
                second_to_last_line_values['requested_by'],
            )
            
            
        else:
            last_line = lines[-1]
            last_line_values = self.parse_csv_line(last_line)
            
            result_string  = "Coin: *{}*\n".format(coin.upper())
            result_string += "Huidige balans: `{}`\n".format(
                last_line_values['balance_in_coins'],
            )
            result_string += "Huidige waarde: `{}` euro\n".format(
                last_line_values['current_value'],
            )
            

        return result_string