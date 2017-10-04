package cryptoBot;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.generics.BotSession;

public class Main {
	private static final Logger LOG = LogManager.getLogger(Main.class);
//	private static final String LOGTAG = "MAIN";
	
	public static void main(String[] args) {
        LOG.info("Starting the bot");
		// Initialize Api Context
        ApiContextInitializer.init();
        
        // Instantiate Telegram Bots API
        TelegramBotsApi botsApi = new TelegramBotsApi();
       
        // Register our bot
        try {
            
        		BotSession botSession = botsApi.registerBot(new CryptoBot());
        		
        		// automatic update the wallet on a hourly basis
        		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
            ses.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                		LOG.info("Triggered automatic task");
                    CryptoBot sendBot = new CryptoBot();
                    sendBot.automaticStatusUpdatePortfolio();
                }
            }, 0, 1, TimeUnit.HOURS);
            
            // check if the bot is still online
            // somehow this keeps the bot alive
            ScheduledExecutorService ses2 = Executors.newSingleThreadScheduledExecutor();
            ses2.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                		LOG.info("Bot running: {}", botSession.isRunning());
                }
            }, 0, 1, TimeUnit.MINUTES);
                        
            LOG.info("Finished starting the bot");
        		
        } catch (TelegramApiException e) {
            e.printStackTrace();
            LOG.fatal("Error starting api: {}, e");
            System.exit(1);
        }
    }
}