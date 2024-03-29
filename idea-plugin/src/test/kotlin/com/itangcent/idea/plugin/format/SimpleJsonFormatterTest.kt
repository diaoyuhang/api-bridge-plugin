package com.itangcent.idea.plugin.format

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Test case of [SimpleJsonFormatter]
 */
internal class SimpleJsonFormatterTest : MessageFormatterTest() {

    override val formatClass get() = SimpleJsonFormatter::class

    @Test
    fun testFormat() {
        assertEquals("{\n" +
                "  \"string\": \"abc\",\n" +
                "  \"int\": 1,\n" +
                "  \"1\": \"int\",\n" +
                "  \"null\": null,\n" +
                "  \"null\": \"null\",\n" +
                "  \"array\": [\n" +
                "    \"def\",\n" +
                "    2,\n" +
                "    {}\n" +
                "  ],\n" +
                "  \"list\": [\n" +
                "    \"ghi\",\n" +
                "    3,\n" +
                "    {\n" +
                "      \"x\": 1,\n" +
                "      \"2\": \"y\"\n" +
                "    },\n" +
                "    []\n" +
                "  ],\n" +
                "  \"map\": {\n" +
                "    \"x\": 1,\n" +
                "    \"2\": \"y\",\n" +
                "    \"empty\": []\n" +
                "  },\n" +
                "  \"any\": {}\n" +
                "}",
                messageFormatter.format(model))
        assertEquals("{\n" +
                "  \"string\": \"abc\",\n" +
                "  \"int\": 1,\n" +
                "  \"1\": \"int\",\n" +
                "  \"null\": null,\n" +
                "  \"null\": \"null\",\n" +
                "  \"array\": [\n" +
                "    \"def\",\n" +
                "    2,\n" +
                "    {}\n" +
                "  ],\n" +
                "  \"list\": [\n" +
                "    \"ghi\",\n" +
                "    3,\n" +
                "    {\n" +
                "      \"x\": 1,\n" +
                "      \"2\": \"y\"\n" +
                "    },\n" +
                "    []\n" +
                "  ],\n" +
                "  \"map\": {\n" +
                "    \"x\": 1,\n" +
                "    \"2\": \"y\",\n" +
                "    \"empty\": []\n" +
                "  },\n" +
                "  \"any\": {}\n" +
                "}",
                messageFormatter.format(model), "test model")
    }
}