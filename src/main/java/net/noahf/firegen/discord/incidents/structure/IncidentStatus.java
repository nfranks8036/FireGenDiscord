//package net.noahf.firegen.discord.incidents.structure;
//
//import lombok.Getter;
//
//public enum IncidentStatus {
//
//    PENDING("\uD83D\uDD35"),
//
//    ACTIVE("\uD83D\uDFE2"),
//
//    CLOSED("⚫"),
//
//    TIMED_OUT("\uD83D\uDD34");
//
//    private final @Getter String emoji;
//    IncidentStatus(String emoji) {
//        this.emoji =  emoji;
//    }
//
//    public IncidentStatus opposite(Incident incident) {
//        return switch (this) {
//            case PENDING, ACTIVE -> CLOSED;
//            case CLOSED, TIMED_OUT -> (incident.getAgencies().isEmpty() ? PENDING : ACTIVE);
//        };
//    }
//
//    public boolean isActive() {
//        return (this == PENDING || this == ACTIVE);
//    }
//
//}
