package com.autoparts.autoparts_system;

import com.autoparts.autoparts_system.controller.VinRequestController;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class VinRequestControllerTest {

    @Test
    void testIsValidVin() throws Exception {
        // Получаем приватный метод isValidVin через рефлексию
        VinRequestController controller = new VinRequestController();
        Method method = VinRequestController.class.getDeclaredMethod("isValidVin", String.class);
        method.setAccessible(true);

        // Валидные VIN (17 символов, только латиница и цифры, без I, O, Q)
        assertTrue((boolean) method.invoke(controller, "XTA12345678901234"));
        assertTrue((boolean) method.invoke(controller, "WDB12345678901234"));
        assertTrue((boolean) method.invoke(controller, "1HGCM82633A123456"));
        assertTrue((boolean) method.invoke(controller, "JHMGD18503S123456"));

        // Невалидные VIN
        assertFalse((boolean) method.invoke(controller, (Object) null));
        assertFalse((boolean) method.invoke(controller, ""));
        assertFalse((boolean) method.invoke(controller, "короткий"));
        assertFalse((boolean) method.invoke(controller, "XTA1234567890123")); // 16 символов
        assertFalse((boolean) method.invoke(controller, "XTA123456789012345")); // 18 символов
        assertFalse((boolean) method.invoke(controller, "XTA1234567890123I")); // содержит I
        assertFalse((boolean) method.invoke(controller, "XTA1234567890123O")); // содержит O
        assertFalse((boolean) method.invoke(controller, "XTA1234567890123Q")); // содержит Q
        assertFalse((boolean) method.invoke(controller, "XTА12345678901234")); // русская А вместо латинской
    }
}