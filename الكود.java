import java.util.EnumSet;

public class CampusIncidentVault {
    enum Permission { VIEW_OWN, VIEW_ALL, CLOSE_INCIDENT }
    enum Role { STUDENT, ANALYST, ADMIN }

    static final class User {
        private final String id;
        private final Role role;
        User(String id, Role role) {
            this.id = id;
            this.role = role;
        }
        String id() { return id; }
        Role role() { return role; }
    }

    static final class Incident {
        private final String number;
        private final String ownerId;
        private String status = "OPEN";
        Incident(String number, String ownerId) {
            this.number = number;
            this.ownerId = ownerId;
        }
        String number() { return number; }
        String status() { return status; }
        private void close() { status = "CLOSED"; }
    }

    static final class AccessPolicy {
        private EnumSet<Permission> permissions(Role role) {
            switch (role) {
                case STUDENT: return EnumSet.of(Permission.VIEW_OWN);
                case ANALYST: return EnumSet.of(Permission.VIEW_ALL);
                case ADMIN: return EnumSet.of(Permission.VIEW_ALL,
                                               Permission.CLOSE_INCIDENT);
                default: return EnumSet.noneOf(Permission.class);
            }
        }
        boolean canView(User user, Incident incident) {
            EnumSet<Permission> rights = permissions(user.role());
            return rights.contains(Permission.VIEW_ALL)
                || (rights.contains(Permission.VIEW_OWN)
                    && user.id().equals(incident.ownerId));
        }
        boolean canClose(User user) {
            return permissions(user.role()).contains(Permission.CLOSE_INCIDENT);
        }
    }

    static final class IncidentService {
        private final AccessPolicy policy = new AccessPolicy();
        String view(User user, Incident incident) {
            if (!policy.canView(user, incident)) return "DENIED";
            return "ALLOWED (status=" + incident.status() + ")";
        }
        String close(User user, Incident incident) {
            if (!policy.canClose(user)) return "DENIED";
            incident.close();
            return "ALLOWED (status=" + incident.status() + ")";
        }
    }

    public static void main(String[] args) {
        User sara = new User("S-101", Role.STUDENT);
        User ali = new User("S-202", Role.STUDENT);
        User analyst = new User("A-007", Role.ANALYST);
        User admin = new User("D-001", Role.ADMIN);
        Incident ticket = new Incident("INC-47", sara.id());
        IncidentService service = new IncidentService();

        System.out.println("Campus Incident Vault | " + ticket.number());
        System.out.println("01 Owner views:    " + service.view(sara, ticket));
        System.out.println("02 Other views:    " + service.view(ali, ticket));
        System.out.println("03 Analyst views:  " + service.view(analyst, ticket));
        System.out.println("04 Analyst closes: " + service.close(analyst, ticket));
        System.out.println("05 Admin closes:   " + service.close(admin, ticket));
        System.out.println("06 Owner rechecks: " + service.view(sara, ticket));
    }
}
