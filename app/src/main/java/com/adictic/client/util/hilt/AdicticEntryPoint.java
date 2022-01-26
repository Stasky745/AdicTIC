package com.adictic.client.util.hilt;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@EntryPoint
@InstallIn(SingletonComponent.class)
public interface AdicticEntryPoint {
    AdicticRepository getRepository();
}
