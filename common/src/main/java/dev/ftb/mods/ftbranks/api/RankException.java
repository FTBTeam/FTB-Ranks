package dev.ftb.mods.ftbranks.api;

/**
 * General exception for any exceptional conditions, usually creation failures.
 */
public class RankException extends RuntimeException {
    public RankException(String message) {
        super(message);
    }
}
