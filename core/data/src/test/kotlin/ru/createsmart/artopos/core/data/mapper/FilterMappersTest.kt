package ru.createsmart.artopos.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.createsmart.artopos.core.database.model.FilterItemDBO
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.network.model.FilterItemDTO

class FilterMappersTest {

    @Test
    fun `map FilterDTO to FilterDBO correctly`() {
        val dto = FilterItemDTO(
            id = 123,
            name = "Paintings",
            count = 500,
            order = null,
        )
        val type = FilterType.CLASSIFICATION

        val dbo = dto.toDBO(type)

        assertEquals(123L, dbo.id)
        assertEquals("Paintings", dbo.name)
        assertEquals("CLASSIFICATION", dbo.type)
        assertEquals(500, dbo.count)
        assertEquals(null, dbo.order)
    }

    @Test
    fun `dbo to domain mapping converts string type to enum`() {
        val dbo = FilterItemDBO(
            uId = 1,
            id = 123,
            type = "CENTURY",
            name = "19th century",
            count = 1000,
            order = 45,
        )

        val domain = dbo.toDomain()

        assertEquals(123L, domain.id)
        assertEquals(FilterType.CENTURY, domain.type)
        assertEquals(45, domain.order)
    }
}
