package ru.effectivegroup.client.algoil.bots;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.algoil.Instrument;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/BotLogger.class */
public class BotLogger {
    private Instrument instrument;
    private final Logger logger = LogManager.getLogger("algoil");

    public BotLogger(Instrument instrument) {
        this.instrument = instrument;
    }

    public void debug(String template, Object arg) {
        this.logger.debug(enrichTemplate(template), arg);
    }

    public void debug(String template, Object arg1, Object arg2) {
        this.logger.debug(enrichTemplate(template), arg1, arg2);
    }

    public void debug(String template, Object arg1, Object arg2, Object arg3) {
        this.logger.debug(enrichTemplate(template), arg1, arg2, arg3);
    }

    public void debug(String template, Object arg1, Object arg2, Object arg3, Object arg4) {
        this.logger.debug(enrichTemplate(template), arg1, arg2, arg3, arg4);
    }

    public void debug(String template, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        this.logger.debug(enrichTemplate(template), arg1, arg2, arg3, arg4, arg5);
    }

    public void info(String template) {
        this.logger.info(enrichTemplate(template));
    }

    public void info(String template, Object arg) {
        this.logger.info(enrichTemplate(template), arg);
    }

    public void info(String template, Object arg1, Object arg2) {
        this.logger.info(enrichTemplate(template), arg1, arg2);
    }

    public void info(String template, Object arg1, Object arg2, Object arg3) {
        this.logger.info(enrichTemplate(template), arg1, arg2, arg3);
    }

    public void info(String template, Object arg1, Object arg2, Object arg3, Object arg4) {
        this.logger.info(enrichTemplate(template), arg1, arg2, arg3, arg4);
    }

    public void info(String template, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        this.logger.info(enrichTemplate(template), arg1, arg2, arg3, arg4, arg5);
    }

    public void warn(String template, Object arg) {
        this.logger.warn(enrichTemplate(template), arg);
    }

    public void warn(String template, Object arg1, Object arg2) {
        this.logger.warn(enrichTemplate(template), arg1, arg2);
    }

    public void warn(String template, Object arg1, Object arg2, Object arg3) {
        this.logger.warn(enrichTemplate(template), arg1, arg2, arg3);
    }

    public void warn(String template, Object arg1, Object arg2, Object arg3, Object arg4) {
        this.logger.warn(enrichTemplate(template), arg1, arg2, arg3, arg4);
    }

    public void warn(String template, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        this.logger.warn(enrichTemplate(template), arg1, arg2, arg3, arg4, arg5);
    }

    public void error(String template, Object arg) {
        this.logger.error(enrichTemplate(template), arg);
    }

    public void error(String template, Object arg1, Object arg2) {
        this.logger.error(enrichTemplate(template), arg1, arg2);
    }

    public void error(String template, Object arg1, Object arg2, Object arg3) {
        this.logger.error(enrichTemplate(template), arg1, arg2, arg3);
    }

    public void error(String template, Object arg1, Object arg2, Object arg3, Object arg4) {
        this.logger.error(enrichTemplate(template), arg1, arg2, arg3, arg4);
    }

    public void error(String template, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        this.logger.error(enrichTemplate(template), arg1, arg2, arg3, arg4, arg5);
    }

    public void fatal(String template) {
        this.logger.fatal(enrichTemplate(template));
    }

    public void fatal(String template, Object arg) {
        this.logger.fatal(enrichTemplate(template), arg);
    }

    public void fatal(String template, Object arg1, Object arg2) {
        this.logger.fatal(enrichTemplate(template), arg1, arg2);
    }

    public void fatal(String template, Object arg1, Object arg2, Object arg3) {
        this.logger.fatal(enrichTemplate(template), arg1, arg2, arg3);
    }

    public void fatal(String template, Object arg1, Object arg2, Object arg3, Object arg4) {
        this.logger.fatal(enrichTemplate(template), arg1, arg2, arg3, arg4);
    }

    public void fatal(String template, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        this.logger.fatal(enrichTemplate(template), arg1, arg2, arg3, arg4, arg5);
    }

    private String enrichTemplate(String template) {
        return String.format("BOT %s %s", this.instrument.getCode(), template);
    }
}
