package com.example.cameragalleryapp;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.EspressoKey;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

// Take a photo and labels it with a caption and then searches for that caption
@RunWith(AndroidJUnit4.class)
public class UICaptionTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    @Test
    public void ensureCaptionSearchWorks() {
        onView(withId(R.id.filterPhotoButton)).perform(click()); // first press the filter button
        onView(withId(R.id.keywordsEditText)).perform(typeText("A"),closeSoftKeyboard()); //type in the caption to be found and close keyboard
        onView(withId(R.id.searchCaptionButton)).perform(click()); // press enter
        onView(withId(R.id.captionTextView)).check(matches(withText("A"))); // perform the comparison
    }
}