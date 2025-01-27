package domain;

public enum DefaultStrategy {
    SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS(false, false),
    IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING(false, false),
    FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS(true, false),
    FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS(false, true);

    private final boolean requiresTot;
    private final boolean requiresMaxConsecSkipped;

    DefaultStrategy(boolean requiresTot, boolean requiresMaxConsecSkipped) {
        this.requiresTot = requiresTot;
        this.requiresMaxConsecSkipped = requiresMaxConsecSkipped;
    }

    public boolean requiresTot() {
        return requiresTot;
    }

    public boolean requiresMaxConsecSkipped() {
        return requiresMaxConsecSkipped;
    }
}
