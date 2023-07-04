package com.thoughtworks.go.scm.plugin.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilTest {

    @Test
    public void shouldRedactText() {
        assertThat(StringUtil.replaceSecretText("hello world hello", List.of("hello", "world"))).isEqualTo("****** ****** ******");
    }

    @Test
    public void shouldRedactTextInOrder() {
        assertThat(StringUtil.replaceSecretText("user userpassword", List.of("userpassword", "user"))).isEqualTo("****** ******");
        assertThat(StringUtil.replaceSecretText("userpassword user", List.of("userpassword", "user"))).isEqualTo("****** ******");
        assertThat(StringUtil.replaceSecretText("user userpassword", List.of("user", "userpassword"))).isEqualTo("****** ******password");
        assertThat(StringUtil.replaceSecretText("userpassword user", List.of("user", "userpassword"))).isEqualTo("******password ******");
    }

    @Test
    public void shouldRedactTextWithoutSecrets() {
        assertThat(StringUtil.replaceSecretText("hello world", List.of())).isEqualTo("hello world");
        assertThat(StringUtil.replaceSecretText("hello world", null)).isEqualTo("hello world");
        assertThat(StringUtil.replaceSecretText("hello world", Arrays.asList(null, "", " "))).isEqualTo("hello world");
        assertThat(StringUtil.replaceSecretText("", Arrays.asList(null, "", " "))).isEqualTo("");
        assertThat(StringUtil.replaceSecretText(null, Arrays.asList(null, "", " "))).isEqualTo(null);
    }
}