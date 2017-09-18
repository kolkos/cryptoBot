"""
File:         Bot.py

This file contains the Bot class. This class is used to handle chat messages.

Class: Bot

"""

import sys
import os
import time
import datetime
from General import General
import telepot
import telepot.helper
from telepot.loop import MessageLoop
from telepot.delegate import (
    per_chat_id,
    per_inline_from_id,
    create_open,
    pave_event_space,
    intercept_callback_query_origin,
    include_callback_query_chat_id
)
import TelegramChatHandler


class Bot(telepot.helper.ChatHandler):
    """
    The Bot Class
    """
    def __init__(self):
        self.general = General()
        self.cryptoBot = object

        self.start_bot()
        while 1:
            # update the pid file every 10 seconds
            time.sleep(30)
            

    def start_bot(self):
        """Function to start the bot"""
        self.cryptoBot = telepot.DelegatorBot(self.general.config.get("Bot", "token"), [
            include_callback_query_chat_id(
                pave_event_space()
            )
            (
                per_chat_id(),
                create_open,
                TelegramChatHandler.TelegramChatHandler,
                timeout=30
            ),
        ])
        #self.bot.message_loop(run_forever='Listening ...')
        # running the bot as a thread to prevent blocking other actions
        # this way we can update the pid file every x seconds
        MessageLoop(self.cryptoBot).run_as_thread()

        print 'Listening ...'
