package ru.netology.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.netology.data.DataHelper;
import ru.netology.data.SQLHelper;
import ru.netology.page.PaymentPage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;

import static com.codeborne.selenide.Selenide.closeWebDriver;
import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PaymentTest {

    @Test
    void shouldApproveCardPayment() throws IOException {
        SQLHelper sqlHelper = createSqlHelper();
        var paymentIdsBefore = sqlHelper.getPaymentIds();
        var card = DataHelper.getApprovedCard();

        open("http://localhost:8080");

        PaymentPage paymentPage = new PaymentPage();
        paymentPage.openCardPayment();
        paymentPage.fillForm(card);
        paymentPage.submit();
        paymentPage.shouldShowSuccess();

        var newPaymentIds = new ArrayList<>(sqlHelper.getPaymentIds());
        newPaymentIds.removeAll(paymentIdsBefore);

        assertEquals(1, newPaymentIds.size(),
                "После оплаты должна появиться одна новая запись");
        assertEquals("APPROVED",
                sqlHelper.getPaymentStatus(newPaymentIds.get(0)),
                "Статус новой оплаты");

        String cardNumber = card.getNumber().replace(" ", "");

        for (Object[] row : sqlHelper.getStoredRows()) {
            for (Object value : row) {
                if (value != null) {
                    String storedValue = value.toString();

                    assertFalse(
                            storedValue.replace(" ", "").contains(cardNumber),
                            "В БД не должен сохраняться номер карты");

                    assertNotEquals(card.getCvc(), storedValue,
                            "В БД не должен сохраняться CVC");

                    assertNotEquals(card.getMonth(), storedValue,
                            "В БД не должен сохраняться месяц действия карты");

                    assertNotEquals(card.getYear(), storedValue,
                            "В БД не должен сохраняться год действия карты");

                    assertNotEquals(
                            card.getMonth() + "/" + card.getYear(),
                            storedValue,
                            "В БД не должен сохраняться срок действия карты");
                }
            }
        }
    }

    private SQLHelper createSqlHelper() throws IOException {
        Properties properties = new Properties();

        try (var reader = Files.newBufferedReader(Path.of("application.properties"))) {
            properties.load(reader);
        }

        return new SQLHelper(
                properties.getProperty("spring.datasource.url"),
                properties.getProperty("spring.datasource.username"),
                properties.getProperty("spring.datasource.password")
        );
    }

    @Test
    void shouldShowErrorsWhenCardPaymentFormIsEmpty() {
        open("http://localhost:8080");

        PaymentPage paymentPage = new PaymentPage();
        paymentPage.openCardPayment();
        paymentPage.fillForm(DataHelper.getEmptyCard());
        paymentPage.submit();

        paymentPage.shouldShowFieldError("Номер карты");
        paymentPage.shouldShowFieldError("Месяц");
        paymentPage.shouldShowFieldError("Год");
        paymentPage.shouldShowFieldError("Владелец");
        paymentPage.shouldShowFieldError("CVC/CVV");
    }

    @Test
    void shouldShowErrorWhenCardNumberIsTooShort() {
        open("http://localhost:8080");

        PaymentPage paymentPage = new PaymentPage();
        paymentPage.openCardPayment();
        paymentPage.fillForm(DataHelper.getCardWithShortNumber());
        paymentPage.submit();

        paymentPage.shouldShowFieldError("Номер карты");
    }

    @Test
    void shouldIgnoreLetterInCardNumber() {
        open("http://localhost:8080");

        PaymentPage paymentPage = new PaymentPage();
        paymentPage.openCardPayment();
        paymentPage.fillForm(DataHelper.getCardWithLettersInNumber());

        assertEquals("4444 4444 4444 444", paymentPage.getCardNumber());

        paymentPage.submit();
        paymentPage.shouldShowFieldError("Номер карты");
    }

    @Test
    void shouldShowErrorWhenCardIsExpired() {
        open("http://localhost:8080");

        PaymentPage paymentPage = new PaymentPage();
        paymentPage.openCardPayment();
        paymentPage.fillForm(DataHelper.getExpiredCard());
        paymentPage.submit();

        paymentPage.shouldShowFieldError("Месяц");
    }

    @Test
    void shouldShowErrorWhenCardMonthIsInvalid() {
        open("http://localhost:8080");

        PaymentPage paymentPage = new PaymentPage();
        paymentPage.openCardPayment();
        paymentPage.fillForm(DataHelper.getCardWithInvalidMonth());
        paymentPage.submit();

        paymentPage.shouldShowFieldError("Месяц");
    }

    @Test
    void shouldShowErrorWhenCardYearIsTooShort() {
        open("http://localhost:8080");

        PaymentPage paymentPage = new PaymentPage();
        paymentPage.openCardPayment();
        paymentPage.fillForm(DataHelper.getCardWithShortYear());
        paymentPage.submit();

        paymentPage.shouldShowFieldError("Год");
    }

    @Test
    void shouldShowErrorWhenCvcIsTooShort() {
        open("http://localhost:8080");

        PaymentPage paymentPage = new PaymentPage();
        paymentPage.openCardPayment();
        paymentPage.fillForm(DataHelper.getCardWithShortCvc());
        paymentPage.submit();

        paymentPage.shouldShowFieldError("CVC/CVV");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "!@#$%", "ИВАН ИВАНОВ"})
    void shouldShowErrorWhenCardHolderIsInvalid(String holder) {
        open("http://localhost:8080");

        PaymentPage paymentPage = new PaymentPage();
        paymentPage.openCardPayment();
        paymentPage.fillForm(DataHelper.getCardWithHolder(holder));
        paymentPage.submit();

        paymentPage.shouldShowFieldError("Владелец");
    }

    @Test
    void shouldShowErrorsWhenCreditPaymentFormIsEmpty() {
        open("http://localhost:8080");

        PaymentPage paymentPage = new PaymentPage();
        paymentPage.openCreditPayment();
        paymentPage.fillForm(DataHelper.getEmptyCard());
        paymentPage.submit();

        paymentPage.shouldShowFieldError("Номер карты");
        paymentPage.shouldShowFieldError("Месяц");
        paymentPage.shouldShowFieldError("Год");
        paymentPage.shouldShowFieldError("Владелец");
        paymentPage.shouldShowFieldError("CVC/CVV");
    }

    @Test
    void shouldApproveCreditPayment() throws IOException {
        SQLHelper sqlHelper = createSqlHelper();
        var creditIdsBefore = sqlHelper.getCreditRequestIds();

        open("http://localhost:8080");

        PaymentPage paymentPage = new PaymentPage();
        paymentPage.openCreditPayment();
        paymentPage.fillForm(DataHelper.getApprovedCard());
        paymentPage.submit();
        paymentPage.shouldShowSuccess();

        var newCreditIds = new ArrayList<>(sqlHelper.getCreditRequestIds());
        newCreditIds.removeAll(creditIdsBefore);

        assertEquals(1, newCreditIds.size(),
                "После оформления кредита должна появиться одна новая запись");
        assertEquals("APPROVED",
                sqlHelper.getCreditRequestStatus(newCreditIds.get(0)),
                "Статус новой кредитной заявки");

        assertEquals(1L,
                sqlHelper.getCreditOrderCount(newCreditIds.get(0)),
                "Заказ должен быть связан с кредитной заявкой через credit_id");
    }

    @Test
    void shouldDeclineCardPayment() throws IOException {
        SQLHelper sqlHelper = createSqlHelper();
        var paymentIdsBefore = sqlHelper.getPaymentIds();

        open("http://localhost:8080");

        PaymentPage paymentPage = new PaymentPage();
        paymentPage.openCardPayment();
        paymentPage.fillForm(DataHelper.getDeclinedCard());
        paymentPage.submit();
        paymentPage.shouldShowBankDecline();

        var newPaymentIds = new ArrayList<>(sqlHelper.getPaymentIds());
        newPaymentIds.removeAll(paymentIdsBefore);

        assertEquals(1, newPaymentIds.size(),
                "После отказа должна появиться одна новая запись оплаты");
        assertEquals("DECLINED",
                sqlHelper.getPaymentStatus(newPaymentIds.get(0)),
                "Статус отклонённой оплаты");
    }

    @Test
    void shouldDeclineCreditPayment() throws IOException {
        SQLHelper sqlHelper = createSqlHelper();
        var creditIdsBefore = sqlHelper.getCreditRequestIds();

        open("http://localhost:8080");

        PaymentPage paymentPage = new PaymentPage();
        paymentPage.openCreditPayment();
        paymentPage.fillForm(DataHelper.getDeclinedCard());
        paymentPage.submit();
        paymentPage.shouldShowBankDecline();

        var newCreditIds = new ArrayList<>(sqlHelper.getCreditRequestIds());
        newCreditIds.removeAll(creditIdsBefore);

        assertEquals(1, newCreditIds.size(),
                "После отказа должна появиться одна новая кредитная заявка");
        assertEquals("DECLINED",
                sqlHelper.getCreditRequestStatus(newCreditIds.get(0)),
                "Статус отклонённой кредитной заявки");
    }

    @AfterEach
    void closeBrowser() {
        closeWebDriver();
    }
}