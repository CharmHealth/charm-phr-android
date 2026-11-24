package com.charm.phr.mphr.util


import com.charm.phr.mphr.util.ModelUtil.Companion.isNotJsonNull
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import org.junit.Test
import org.mockito.Mockito.mock

/**
 *   Use Arrange, Act, Assert comments while writing tests
 **/
class ModelUtilTest {

    @Test
    fun `isJsonNotNull should return true for non-null JsonElement`() {
        // Arrange
        val jsonElement = mock(JsonElement::class.java)

        // Act
        val result = jsonElement.isNotJsonNull()

        // Assert
        assertThat(result).isTrue()
    }

    @Test
    fun `isJsonNotNull should return false for null JsonElement`() {
        // Arrange
        val jsonElement: JsonElement? = null

        // Act
        val result = jsonElement.isNotJsonNull()

        // Assert
        assertThat(result).isFalse()
    }

    @Test
    fun `isJsonNotNull should return false for JsonNull`() {
        // Arrange
        val jsonElement: JsonElement? = JsonNull.INSTANCE

        // Act
        val result = jsonElement.isNotJsonNull()

        // Assert
        assertThat(result).isFalse()
    }

    @Test
    fun `parseStringAsJsonObject should return null for blank or null input`() {
        // Arrange
        val nullInput: String? = null
        val blankInput = ""

        // Act
        val resultNullInput = nullInput.parseStringAsJsonObject()
        val resultBlankInput = blankInput.parseStringAsJsonObject()

        // Assert
        assertThat(resultNullInput).isNull()
        assertThat(resultBlankInput).isNull()
    }

    @Test
    fun `parseStringAsJsonObject should return JsonObject for valid JSON string`() {
        // Arrange
        val jsonString = "{\"key\":\"value\"}"

        // Act
        val result = jsonString.parseStringAsJsonObject()

        // Assert
        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(JsonObject::class.java)
        assertThat(result?.get("key")?.asString).isEqualTo("value")
    }

}