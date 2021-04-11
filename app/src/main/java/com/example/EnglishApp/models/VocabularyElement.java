package com.example.EnglishApp.models;

public class VocabularyElement {
    private String wordInEnglish;
    private String translationInRussian;
    VocabularyElement(){}

    public VocabularyElement(String wordInEnglish, String translationInRussian) {
        this.wordInEnglish = wordInEnglish;
        this.translationInRussian = translationInRussian;
    }

    public String getWordInEnglish() {
        return wordInEnglish;
    }

    public void setWordInEnglish(String wordInEnglish) {
        this.wordInEnglish = wordInEnglish;
    }

    public String getTranslationInRussian() {
        return translationInRussian;
    }

    public void setTranslationInRussian(String translationInRussian) {
        this.translationInRussian = translationInRussian;
    }
}
