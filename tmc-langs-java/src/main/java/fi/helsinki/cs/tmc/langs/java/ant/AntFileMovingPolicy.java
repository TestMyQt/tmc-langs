package fi.helsinki.cs.tmc.langs.java.ant;

import fi.helsinki.cs.tmc.langs.sandbox.StudentFileAwareFileMovingPolicy;

import java.nio.file.Path;

public class AntFileMovingPolicy extends StudentFileAwareFileMovingPolicy {

    /**
     * Returns {@code True} for all files in the <tt>projectRoot/src</tt> directory and other
     * files required for building the project.
     *
     * <p>Will NOT return {@code True} for any test files. If test file modification are part
     * of the exercise, those test files are whitelisted as <tt>ExtraStudentFiles</tt> and the
     * decision to move them is made by {@link StudentFileAwareFileMovingPolicy}.
     */
    @Override
    protected boolean shouldMoveFile(Path path) {
        throw new UnsupportedOperationException();
    }
}
