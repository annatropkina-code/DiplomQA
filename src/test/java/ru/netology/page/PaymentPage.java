package ru.netology.page;

import com.codeborne.selenide.SelenideElement;
import ru.netology.data.DataHelper.CardInfo;

import java.time.Duration;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import static com.codeborne.selenide.Condition.text;

import static com.codeborne.selenide.Condition.cssClass;

public class PaymentPage {

    private final SelenideElement form = $("form.form");
    private final SelenideElement successNotification = $(".notification_status_ok");

    public void openCardPayment() {
        $$("button").findBy(exactText("Купить")).click();
        $$("h3").findBy(exactText("Оплата по карте")).shouldBe(visible);
    }

    public void openCreditPayment() {
        $$("button").findBy(exactText("Купить в кредит")).click();
        $$("h3").findBy(exactText("Кредит по данным карты")).shouldBe(visible);
    }

    public void fillForm(CardInfo card) {
        findInput("Номер карты").setValue(card.getNumber());
        findInput("Месяц").setValue(card.getMonth());
        findInput("Год").setValue(card.getYear());
        findInput("Владелец").setValue(card.getHolder());
        findInput("CVC/CVV").setValue(card.getCvc());
    }

    public String getCardNumber() {
        return findInput("Номер карты").getValue();
    }

    public void submit() {
        form.$$("button").findBy(exactText("Продолжить")).click();
    }

    public void shouldShowSuccess() {
        successNotification.shouldBe(visible, Duration.ofSeconds(15));
    }

    public void shouldShowBankDecline() {
        $$(".notification")
                .findBy(visible)
                .shouldHave(
                        text("Банк отказал в проведении операции"),
                        Duration.ofSeconds(15));
    }

    public void shouldShowFieldError(String label) {
        SelenideElement fieldBlock = findFieldBlock(label);

        fieldBlock.parent().shouldHave(cssClass("input_invalid"));
        fieldBlock.$(".input__sub").shouldBe(visible);
    }

    private SelenideElement findInput(String label) {
        return findFieldBlock(label).$("input.input__control");
    }

    private SelenideElement findFieldBlock(String label) {
        return form.$$(".input__top")
                .findBy(exactText(label))
                .parent();
    }
}
