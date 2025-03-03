package ru.effectivegroup.client.algoil.bots.states;

import ru.effectivegroup.client.algoil.bots.BotMode;
import ru.effectivegroup.client.algoil.execution.Order;
import ru.effectivegroup.client.crypto.cipher.CipherHolder;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotStateIdle.class */
public class BotStateIdle extends BotStateBase {
    private Order order;

    public BotStateIdle(BotStateContext context) {
        super("IDLE", context);
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public BotStateKind getStateKind() {
        return BotStateKind.Stopped;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Start() {
        Trace("Start");
        this.context.setNextOrderFirst(true);
        switch (AnonymousClass1.$SwitchMap$ru$effectivegroup$client$algoil$bots$BotMode[this.context.getBotMode().ordinal()]) {
            case 1:
            case 2:
                this.context.setState(new BotStateOverlapWaiting(this.context, true));
                return;
            case CipherHolder.CIPHER_PARAM_ENCRYPTED_SECRET_KEY /* 3 */:
                this.context.setState(new BotStateCatchWaiting(this.context, true));
                return;
            default:
                return;
        }
    }

    /* renamed from: ru.effectivegroup.client.algoil.bots.states.BotStateIdle$1, reason: invalid class name */
    /* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotStateIdle$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ru$effectivegroup$client$algoil$bots$BotMode = new int[BotMode.values().length];

        static {
            try {
                $SwitchMap$ru$effectivegroup$client$algoil$bots$BotMode[BotMode.Overlap.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$algoil$bots$BotMode[BotMode.TurboOverlap.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$algoil$bots$BotMode[BotMode.Catch.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }
}
