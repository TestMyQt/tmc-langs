package fi.helsinki.cs.tmc.langs.sandbox;

import java.nio.file.Path;

/**
 * Specifies what file are to be moved when preparing a student submission for running in the
 * TMC-sandbox.
 */
public interface FileMovingPolicy {

    /**
     * Answers whether a single file should be moved.
     */
    boolean shouldMove(Path path);
}
