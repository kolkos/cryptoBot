"""
File:         Database.py

This file contains the Database class. This class is used to handle interactions with the database.

Class: Database

"""
import time
import sqlite3
from General import General
class Database(object):
    """
    Database class

    1AbmdtWNYFnxWkksAuGx7U8GmiSrpHA6FG

    129TQVAroeehD9fZpzK51NdZGQT4TqifbG

    https://api.blockcypher.com/v1/btc/main/addrs/129TQVAroeehD9fZpzK51NdZGQT4TqifbG/balance



    /q/addressbalance/1EzwoHtiXB4iFwedPr49iywjZn2nnekhoj

    
    """
    def __init__(self):
        self.general = General()
        self.database = self.general.fix_real_path('../cryptoBot.db')
        self.conn = None
        self.cursor = None

    def connect_db(self):
        """
        Method to connect to the database and set the cursor
        """
        kwargs = {
            'class': self.__class__.__name__,
            'method': self.connect_db.__name__,
            'action': "Method called"
        }
        self.general.logger(3, **kwargs)

        start_time = time.time()

        self.conn = sqlite3.connect(self.database)
        self.cursor = self.conn.cursor()
        kwargs = {
            'class': self.__class__.__name__,
            'method': self.connect_db.__name__,
            'result': "Connected succesfully to '" + self.database + "'"
        }

        self.general.logger(
            2,
            **kwargs
        )

        execution_time = time.time() - start_time
        kwargs = {
            'class': self.__class__.__name__,
            'method': self.connect_db.__name__,
            'action': "Method finished",
            'execution_time': execution_time
        }
        self.general.logger(
            3,
            **kwargs
        )