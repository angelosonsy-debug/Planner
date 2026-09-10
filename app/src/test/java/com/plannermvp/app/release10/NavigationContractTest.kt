package com.plannermvp.app.release10

import com.plannermvp.app.navigation.AppDestination
import com.plannermvp.app.navigation.NestedRoute
import org.junit.Assert.*
import org.junit.Test

class NavigationContractTest {

    @Test fun `CALENDAR route is defined and not empty`() {
        assertNotNull(NestedRoute.CALENDAR)
        assertTrue(NestedRoute.CALENDAR.isNotEmpty())
    }

    @Test fun `CALENDAR route differs from top-level destinations`() {
        val topLevelRoutes = AppDestination.values().map { it.route }.toSet()
        assertFalse("Calendar must be nested, not a top-level tab",
            topLevelRoutes.contains(NestedRoute.CALENDAR))
    }

    @Test fun `TODAY is first bottom bar destination`() {
        assertEquals(AppDestination.TODAY, AppDestination.bottomBarOrder.first())
    }

    @Test fun `MORE is last bottom bar destination`() {
        assertEquals(AppDestination.MORE, AppDestination.bottomBarOrder.last())
    }

    @Test fun `bottom bar has exactly 5 destinations`() {
        assertEquals(5, AppDestination.bottomBarOrder.size)
    }

    @Test fun `nested routes do not duplicate top-level routes`() {
        val topLevel = AppDestination.values().map { it.route }.toSet()
        assertFalse(topLevel.contains(NestedRoute.SETTINGS))
        assertFalse(topLevel.contains(NestedRoute.IMPORT))
        assertFalse(topLevel.contains(NestedRoute.BACKUP))
    }
}
