package org.mifos.connector.mtn.Util;
import java.util.UUID;

public class GenerateUUID {
    public static String generateUUID() {
        UUID uuid = UUID.randomUUID();
        return  uuid.toString();
    }
}
