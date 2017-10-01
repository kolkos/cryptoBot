package cryptoBot;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Main {
	private static final Logger LOG = LogManager.getLogger(Main.class);
	
	public static void main(String[] args) {
        LOG.info("Starting the bot");
		// Initialize Api Context
        ApiContextInitializer.init();

        // Instantiate Telegram Bots API
        TelegramBotsApi botsApi = new TelegramBotsApi();

        // Register our bot
        try {
            botsApi.registerBot(new CryptoBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
            LOG.fatal("Error starting api: {}, e");
        }
        
        
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
            		LOG.info("Triggered automatic task");
                CryptoBot sendBot = new CryptoBot();
                sendBot.automaticStatusUpdatePortfolio();
            }
        }, 0, 1, TimeUnit.MINUTES);
        LOG.info("Finished starting the bot");
    }
	
}