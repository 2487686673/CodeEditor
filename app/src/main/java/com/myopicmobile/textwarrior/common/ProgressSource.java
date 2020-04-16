/*
 * Copyright (c) 2011 Tah Wei Hoon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License Version 2.0,
 * with full text available at http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * This software is provided "as is". Use at your own risk.
 */
package com.myopicmobile.textwarrior.common;

/**
 * Represents tasks that carry out long computations
 */
public interface ProgressSource {
    /* Nature of computation tasks */
    int NONE = 0;
    int READ = 1;
    int WRITE = 2;
    int FIND = 4;
    int FIND_BACKWARDS = 8;
    int REPLACE_ALL = 16;
    int ANALYZE_TEXT = 32;
    /* Error codes */
    int ERROR_UNKNOWN = 0;
    int ERROR_OUT_OF_MEMORY = 1;
    int ERROR_INDEX_OUT_OF_RANGE = 2;

	/** Minimum progress value */
    int getMin();

	/** Maximum progress value */
    int getMax();

	/** Current progress value */
    int getCurrent();

	/** Whether computation is done */
    boolean isDone();
	
	/** Aborts computation */
    void forceStop();

	/** Registers observers that will be informed of changes to the progress state */
    void registerObserver(ProgressObserver obsv);

	/** Removes all attached observers */
    void removeObservers();
}
