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
import org.hamcrest.core.IsInstanceOf;
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
import static org.hamcrest.Matchers.startsWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RealtimeConnectedInstrumentationTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void realtimeConnectedInstrumentationTest() {

        /* Open navigation drawer and change to pair device fragment. */
        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction appCompatCheckedTextView = onView(
                allOf(withId(R.id.design_menu_item_text), withText("Pair Device"), isDisplayed()));
        appCompatCheckedTextView.perform(click());

        /* Attempt to connect to logging device (by click). */
        ViewInteraction relativeLayout = onView(
                allOf(childAtPosition(
                        withId(R.id.pairdevice_deviceList),
                        0),
                        isDisplayed()));
        relativeLayout.perform(click());

        /* Check that device status changes to connected. */
        ViewInteraction textView = onView(
                allOf(withId(R.id.textListStatus), withText("connected"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.pairdevice_deviceList),
                                        0),
                                3),
                        isDisplayed()));
        textView.check(matches(withText("connected")));

        /* Open navigation drawer and change to realtime fragment. */
        ViewInteraction appCompatImageButton2 = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        ViewInteraction appCompatCheckedTextView2 = onView(
                allOf(withId(R.id.design_menu_item_text), withText("Real Time"), isDisplayed()));
        appCompatCheckedTextView2.perform(click());

        /*
         * Check that there are headings for main items in the table.
         * Instead of checking for all we are just selecting the main statistics.
         */
        ViewInteraction textView2 = onView(
                allOf(withId(R.id.datalist_name), withText("Yaw"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.TableLayout.class),
                                        0),
                                0),
                        isDisplayed()));
        textView2.check(matches(withText("Yaw")));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.datalist_name), withText("Pitch"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.TableLayout.class),
                                        0),
                                0),
                        isDisplayed()));
        textView3.check(matches(withText("Pitch")));

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.datalist_name), withText("Roll"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.TableLayout.class),
                                        0),
                                0),
                        isDisplayed()));
        textView4.check(matches(withText("Roll")));

        ViewInteraction textView5 = onView(
                allOf(withId(R.id.datalist_name), withText("Latitude"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.TableLayout.class),
                                        0),
                                0),
                        isDisplayed()));
        textView5.check(matches(withText("Latitude")));

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.datalist_name), withText("Longitude"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.TableLayout.class),
                                        0),
                                0),
                        isDisplayed()));
        textView6.check(matches(withText("Longitude")));

        /* Check that reading status textview exists. */
        ViewInteraction textView7 = onView(
                allOf(withId(R.id.realtime_status), withText(startsWith("Reading count: ")),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content_frame),
                                        0),
                                0),
                        isDisplayed()));
        textView7.check(matches(withText(startsWith("Reading count: "))));
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
