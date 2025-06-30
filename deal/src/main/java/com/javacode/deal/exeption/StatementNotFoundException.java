package com.javacode.deal.exeption;

import java.util.UUID;

public class StatementNotFoundException extends Exception{

    public StatementNotFoundException(UUID uuid){
        super("Statement с ID % не найден".formatted(uuid));
    }
}
