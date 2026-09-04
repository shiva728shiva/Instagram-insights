package com.example

import com.example.data.InstagramLinkFetcher
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testExtractShortcode() {
    val url1 = "https://www.instagram.com/reel/C8qA1b2c3d4/"
    assertEquals("C8qA1b2c3d4", InstagramLinkFetcher.extractShortcode(url1))

    val url2 = "https://instagram.com/reels/C8qA1b2c3d4?igsh=MWx..."
    assertEquals("C8qA1b2c3d4", InstagramLinkFetcher.extractShortcode(url2))

    val url3 = "https://www.instagram.com/p/DF123456789/"
    assertEquals("DF123456789", InstagramLinkFetcher.extractShortcode(url3))

    val url4 = "https://www.instagram.com/share/reel/XYZ987654321/"
    assertEquals("XYZ987654321", InstagramLinkFetcher.extractShortcode(url4))

    val directCode = "C8qA1b2c3d4"
    assertEquals("C8qA1b2c3d4", InstagramLinkFetcher.extractShortcode(directCode))
  }
}
