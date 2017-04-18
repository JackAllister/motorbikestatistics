package com.jack.motorbikestatistics;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PairDeviceInstrumentationTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void pairDeviceInstrumentationTest() {

        /* Open navigation drawer/tab. */
        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        /* Check that Pair Device tab exists. */
        ViewInteraction checkedTextView = onView(
                allOf(withId(R.id.design_menu_item_text),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.design_navigation_view),
                                        3),
                                0),
                        isDisplayed()));
        checkedTextView.check(matches(isDisplayed()));

        /* Click pair device item. */
        ViewInteraction appCompatCheckedTextView = onView(
                allOf(withId(R.id.design_menu_item_text), withText("Pair Device"), isDisplayed()));
        appCompatCheckedTextView.perform(click());

        /* Check that search for devices button exists. */
        ViewInteraction toggleButton = onView(
                allOf(withId(R.id.pairdevice_search),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content_frame),
                                        0),
                                2),
                        isDisplayed()));
        toggleButton.check(matches(isDisplayed()));

        /* Check that search button says "Search For Devices" then click it. */
        ViewInteraction toggleButton2 = onView(
                allOf(withId(R.id.pairdevice_search), withText("Search For Devices"), isDisplayed()));
        toggleButton2.perform(click());

        /* Check that search for devices button still exists. */
        ViewInteraction toggleButton3 = onView(
                allOf(withId(R.id.pairdevice_search),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content_frame),
                                        0),
                                2),
                        isDisplayed()));
        toggleButton3.check(matches(isDisplayed()));

        /* Check that LOGGING-DEVICE item exists within found devices. */
        ViewInteraction textView = onView(
                allOf(withId(R.id.textListName), withText("LOGGING-DEVICE"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.pairdevice_deviceList),
                                        0),
                                1),
                        isDisplayed()));
        textView.check(matches(withText("LOGGING-DEVICE")));

        /* Check that search button now says "Stop Device Search" then click it. */
        ViewInteraction toggleButton4 = onView(
                allOf(withId(R.id.pairdevice_search), withText("Stop Device Search"), isDisplayed()));
        toggleButton4.perform(click());

        /* Check that search for devices button still exists. */
        ViewInteraction toggleButton5 = onView(
                allOf(withId(R.id.pairdevice_search),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content_frame),
                                        0),
                                2),
                        isDisplayed()));
        toggleButton5.check(matches(isDisplayed()));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
