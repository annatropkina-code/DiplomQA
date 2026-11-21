package ru.netology.data;

import lombok.Value;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class DataHelper {

    private DataHelper() {

    }

    @Value
    public static class CardInfo {
        String number;
        String month;
        String year;
        String holder;
        String cvc;
    }

    public static CardInfo getApprovedCard() {
        return createCard("4444 4444 4444 4441");
    }

    public static CardInfo getDeclinedCard() {
        return createCard("4444 4444 4444 4442");
    }

    public static CardInfo getCardWithShortNumber() {
        return createCard("4444 4444 4444 444");
    }

    public static CardInfo getCardWithLettersInNumber() {
        return createCard("4444 4444 4444 444A");
    }

    public static CardInfo getExpiredCard() {
        CardInfo card = getApprovedCard();

        YearMonth expirationDate = YearMonth.now().minusMonths(1);

        String month = expirationDate.format(
                DateTimeFormatter.ofPattern("MM"));
        String year = expirationDate.format(
                DateTimeFormatter.ofPattern("yy"));

        return new CardInfo(
                card.getNumber(),
                month,
                year,
                card.getHolder(),
                card.getCvc()
        );
    }

    public static CardInfo getCardWithInvalidMonth() {
        CardInfo card = getApprovedCard();

        return new CardInfo(
                card.getNumber(),
                "13",
                card.getYear(),
                card.getHolder(),
                card.getCvc()
        );
    }

    public static CardInfo getCardWithShortCvc() {
        CardInfo card = getApprovedCard();

        return new CardInfo(
                card.getNumber(),
                card.getMonth(),
                card.getYear(),
                card.getHolder(),
                "12"
        );
    }

    public static CardInfo getCardWithHolder(String holder) {
        CardInfo card = getApprovedCard();

        return new CardInfo(
                card.getNumber(),
                card.getMonth(),
                card.getYear(),
                holder,
                card.getCvc()
        );
    }

    public static CardInfo getCardWithShortYear() {
        CardInfo card = getApprovedCard();

        return new CardInfo(
                card.getNumber(),
                card.getMonth(),
                "2",
                card.getHolder(),
                card.getCvc()
        );
    }

    public static CardInfo getEmptyCard() {
        return new CardInfo("", "", "", "", "");
    }


    private static CardInfo createCard(String number) {
        YearMonth expirationDate = YearMonth.now().plusYears(1);

        String month = expirationDate.format(
                DateTimeFormatter.ofPattern("MM"));
        String year = expirationDate.format(
                DateTimeFormatter.ofPattern("yy"));

        return new CardInfo(
                number,
                month,
                year,
                "IVAN IVANOV",
                "123");
    }
}