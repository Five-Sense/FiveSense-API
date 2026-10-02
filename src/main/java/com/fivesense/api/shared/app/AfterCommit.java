package com.fivesense.api.shared.app;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public final class AfterCommit {
    private AfterCommit() {}
    public static void run(Runnable action){
        if(!TransactionSynchronizationManager.isSynchronizationActive()){action.run();return;}
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){@Override public void afterCommit(){action.run();}});
    }
}
