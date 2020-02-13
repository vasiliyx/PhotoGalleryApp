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
public class UILocationTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    @Test
    public void ensureCaptionSearchWorks() {
        onView(withId(R.id.filterPhotoButton)).perform(click()); // first press the filter button
        onView(withId(R.id.topLeftLatEditText)).perform(typeText("52")); //type in the coordinates for BC
        onView(withId(R.id.topLeftLongEditText)).perform(typeText("-130")); //type in the coordinates for BC
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.bottomRightLatEditText)).perform(typeText("40")); //type in the coordinates for BC
        onView(withId(R.id.bottomRightLongEditText)).perform(typeText("-110"),closeSoftKeyboard()); //type in the coordinates for BC
        onView(withId(R.id.searchLocationButton)).perform(click()); // press enter
    }
}