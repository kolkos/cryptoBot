"""
File:         General.py

This file contains the General class. This class contains general methods used by the bot.

Class: General

"""
import os
import ConfigParser
import io
import time
from datetime import datetime
class General(object):
    """
    The General class
    """
    def __init__(self):
        self.log_file = self.fix_real_path('../cryptoBot.log')
        self.config_file = self.fix_real_path('../config.ini')
        self.config = {}
        self.load_config()

    @staticmethod
    def fix_real_path(file_name):
        """
        Retrieves the full path of the file
        """
        real_path = os.path.join(
            os.path.abspath(
                os.path.dirname(
                    __file__
                )
            ),
            file_name
        )
        return real_path

    def load_config(self):
        """
        Method to load the config file
        """
        with open(self.config_file) as f:
            file_contents = f.read()
        self.config = ConfigParser.RawConfigParser(allow_no_value=True)
        self.config.readfp(io.BytesIO(file_contents))

    def logger(self, priority, **kwargs):
        """
        Method to write to the log file.abs

        There are currently four priorities defined:
        0 = Error   -> Something went wrong, code could not continue.
        1 = Warning -> Something bad happened, the code could continue,
                       results may be nog what is expected.
        2 = Info    -> Comfirmation that a action is executed successfully
        3 = Debug   -> For debugging purposes

        This method checks the configuration file if the line should be logged.

        :param priority: The number must match one of the priorities above
        :param class_name: The name of the class from where the logger is called
        :param method_name: The name of the method from where the logger is called
        :param text_to_log: The actual text to log
        """
        # check if the line should be logged
        if priority <= int(self.config.get('General', 'log_level')):
            timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")

            # translate the priority to a readable text
            priority_def = ['Error', 'Warning', 'Info', 'Debug']
            priority_text = priority_def[priority]

            # First get the name of the class from kwargs
            # if not set, fill with default value
            # using pop it will remove this key from the dictionary
            class_name = kwargs.pop('class', "Class name not given")
            
            # now do the same for the method name
            method_name = kwargs.pop('method', "Method name not given")

            # now prepare the string with all aditional information
            # this method loops trough the keys (which arent popped)
            text_to_log = ""
            for key in kwargs:
                text_to_log += ", " + key + '"' + str(kwargs[key]) + '"'

            # define the log string pattern
            pattern = '{}, priority="{}", class="{}", method="{}"{}\n'

            # create the string to log
            log_string = pattern.format(
                timestamp,
                priority_text,
                class_name,
                method_name,
                text_to_log
            )

            # write to the log file
            try:
                log = open(self.log_file, 'a+')
                log.write(log_string)
                log.close()
            except IOError as error:
                print error

        return

    def append_to_file(self, file_name, string):
        """
        Simple method to append a string to a text file
        """
        kwargs = {
            'class': self.__class__.__name__,
            'method': self.append_to_file.__name__,
            'action': "Method called",
            'file_name': file_name,
            'string': string
        }
        start_time = time.time()
        self.logger(3, **kwargs)
        try:
            f = open(file_name, 'a+')
            f.write(string)
            f.close()
        except IOError as error:
            kwargs = {
                'class': self.__class__.__name__,
                'method': self.append_to_file.__name__,
                'action': "Method called",
                'file_name': file_name,
                'string': string,
                'error': str(error)
            }
            self.logger(1, **kwargs)

        execution_time = time.time() - start_time
        kwargs = {
            'class': self.__class__.__name__,
            'method': self.append_to_file.__name__,
            'action': "Method finished",
            'execution_time': execution_time
        }
        self.logger(3, **kwargs)
