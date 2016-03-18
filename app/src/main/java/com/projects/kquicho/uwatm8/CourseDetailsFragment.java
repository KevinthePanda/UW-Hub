package com.projects.kquicho.uwatm8;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.projects.kquicho.uw_api_client.Core.APIResult;
import com.projects.kquicho.uw_api_client.Core.JSONDownloader;
import com.projects.kquicho.uw_api_client.Core.UWOpenDataAPI;
import com.projects.kquicho.uw_api_client.Course.CourseDetails;
import com.projects.kquicho.uw_api_client.Course.CourseParser;

/**
 * Created by Kevin Quicho on 3/16/2016.
 */
public class CourseDetailsFragment extends Fragment implements JSONDownloader.onDownloadListener{
    public static final String TAG = "courseDetailsFragment";
    public static final String SUBJECT_TAG = "subject";
    public static final String CATALOG_NUMBER_TAG = "catalogNumber";
    private String mSubject;
    private String mCatalogNumber;
    private CourseParser mCoursesParser = new CourseParser();
    private String mUrl;
    private CourseDetails mCourseDetails;
    private View mView;


    public static CourseDetailsFragment newInstance(String subject, String catalogNumber) {

        Bundle args = new Bundle();
        args.putString(SUBJECT_TAG, subject);
        args.putString(CATALOG_NUMBER_TAG, catalogNumber);
        CourseDetailsFragment fragment = new CourseDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mSubject = args.getString(SUBJECT_TAG);
        mCatalogNumber = args.getString(CATALOG_NUMBER_TAG);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        mCoursesParser.setParseType(CourseParser.ParseType.COURSE_DETAILS.ordinal());
        mUrl = UWOpenDataAPI.buildURL(String.format(mCoursesParser.getEndPoint(), mSubject, mCatalogNumber));

        JSONDownloader downloader = new JSONDownloader(mUrl);
        downloader.setOnDownloadListener(this);
        downloader.start();

    }


    @Override
    public void onDownloadFail(String givenURL, int index) {
        Log.e(TAG, "Download failed.. url = " + givenURL);
    }

    @Override
    public void onDownloadComplete(APIResult apiResult) {
        mCoursesParser.setAPIResult(apiResult);
        mCoursesParser.parseJSON();
        mCourseDetails = mCoursesParser.getCourseDetail();

        android.os.Handler handler = new android.os.Handler(getActivity().getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ExpandableTextView description = (ExpandableTextView)mView.findViewById(R.id.expandable_description);
                TextView prerequisites = (TextView)mView.findViewById(R.id.prerequisites);
                TextView antirequisites = (TextView)mView.findViewById(R.id.antirequisites);
                TextView corequisites = (TextView)mView.findViewById(R.id.corequisites);
                TextView crossListings = (TextView)mView.findViewById(R.id.cross_listings);
                TextView termsOffered = (TextView)mView.findViewById(R.id.terms_offered);
                TextView instructions = (TextView)mView.findViewById(R.id.instructions);
                TextView offerings = (TextView)mView.findViewById(R.id.offerings);
                TextView consent = (TextView)mView.findViewById(R.id.consent);

                String descriptionText = mCourseDetails.getDescription();
                if(descriptionText != null && !descriptionText.equals("") && !descriptionText.trim().equals("null")) {
                    description.setText(descriptionText);
                }else{
                    description.setText(getString(R.string.default_string));
                }
                final String preqText = mCourseDetails.getPrerequisites();
                if(preqText != null && !preqText.equals("") && !preqText.equals("null")){
                    prerequisites.setText(preqText);
                }
                String antiReqText = mCourseDetails.getAntirequisites();
                if(antiReqText != null && !antiReqText.equals("") && !antiReqText.equals("null")) {
                    antirequisites.setText(antiReqText);
                }
                String coReqText = mCourseDetails.getCorequisites();
                if(coReqText != null && !coReqText.equals("") && !coReqText.equals("null")) {
                    corequisites.setText(coReqText);
                    corequisites.setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.title_corequisites).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.separator_corequisites).setVisibility(View.VISIBLE);
                }
                String crossListingsText = mCourseDetails.getCrossListings();
                if(crossListingsText != null && !crossListingsText.equals("") && !crossListingsText.equals("null")) {
                    crossListings.setText(crossListingsText);
                    crossListings.setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.title_cross_listings).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.separator_cross_listings).setVisibility(View.VISIBLE);
                }
                String termsOfferedText = mCourseDetails.getTermsOffered();
                if(termsOfferedText != null && !termsOfferedText.equals("null")) {
                    if (!termsOfferedText.equals("")){
                        termsOffered.setText(termsOfferedText);
                    }else{
                        termsOffered.setText(getString(R.string.currently_not_offered));
                    }
                }
                String instructionsText = mCourseDetails.getInstructions();
                if(instructionsText != null && !instructionsText.equals("") && !instructionsText.equals("null")) {
                    instructions.setText(instructionsText);
                }

                if (mCourseDetails.getOnlineOnly()){
                    offerings.setText(getString(R.string.online_only));
                }else if(mCourseDetails.getStJeromesOnly()){
                    offerings.setText(getString(R.string.st_jeromes_only));
                }else if(mCourseDetails.getRenisonOnly()){
                    offerings.setText(getString(R.string.renison_only));
                }else if(mCourseDetails.getConradGrebelOnly()){
                    offerings.setText(getString(R.string.conrad_grebel_only));
                }else{
                    String mainCampus = getString(R.string.main_campus);
                    String offeringsText = "";
                    if(mCourseDetails.getOnline()){
                        offeringsText += getString(R.string.online);
                    }
                    if(mCourseDetails.getStJeromes()){
                        if(!offeringsText.equals("")){
                            offeringsText += ", ";
                        }
                        offeringsText += getString(R.string.st_jeromes);
                    }
                    if(mCourseDetails.getRenison()){
                        if(!offeringsText.equals("")){
                            offeringsText += ", ";
                        }
                        offeringsText += getString(R.string.renison);
                    }

                    if(mCourseDetails.getConradGrebel()){
                        if(!offeringsText.equals("")){
                            offeringsText += ", ";
                        }
                        offeringsText += getString(R.string.conrad_grebel);
                    }
                    if(!offeringsText.equals("")){
                        offerings.setText(offeringsText);
                    }else{
                        offerings.setText(mainCampus);
                    }
                }

                String consentText = "";
                if(mCourseDetails.getNeedsDepartmentConsent()){
                    consentText += getString(R.string.department);
                }
                if(mCourseDetails.getNeedsInstructorConsent()){
                    if(!consentText.equals("")){
                        consentText += ", ";
                    }
                    consentText += getString(R.string.instructor);
                }
                if(!consentText.equals("")){
                    consent.setText(consentText);
                    consent.setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.title_consent).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.separator_consent).setVisibility(View.VISIBLE);
                }
               /* final ProgressBar progressBar = (ProgressBar) mView.findViewById(R.id.progressBar);
                final TextView enrollmentTotal = (TextView) mView.findViewById(R.id.enrollment_total);

                progressBar.setMax(300);
                ObjectAnimator animation = ObjectAnimator.ofInt (progressBar, "progress", 0, 260); // see this max value coming back here, we animale towards that value
                animation.setDuration(1000); //in milliseconds
                animation.setInterpolator(new DecelerateInterpolator());
                animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Integer current = (Integer)animation.getAnimatedValue();
                        enrollmentTotal.setText(current.toString());
                    }
                });
                animation.start();

                ArgbEvaluator evaluator = new ArgbEvaluator();
                ValueAnimator animator = new ValueAnimator();
                animator.setIntValues(Color.parseColor("#ff99cc00"), Color.parseColor("#ffff8800"));
                animator.setEvaluator(evaluator);
                animator.setDuration(1000);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int color = (int) animation.getAnimatedValue();
                        progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                    }
                });
                animator.start();


                ValueAnimator textAnimator = new ValueAnimator();
                textAnimator.setObjectValues("0", "260");
                textAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        enrollmentTotal.setText((CharSequence) animation.getAnimatedValue());
                    }
                });
                textAnimator.setEvaluator(new TypeEvaluator<CharSequence>() {
                    public CharSequence evaluate(float fraction,
                                                 CharSequence startValue, CharSequence endValue) {
                        return String.valueOf(Math.round(Integer.valueOf(endValue.toString()) * fraction)) + "/" + "300";
                    }
                });

                textAnimator.setDuration(1000);
                textAnimator.setInterpolator(new DecelerateInterpolator());
                //textAnimator.start();*/


            }
        };
        handler.post(runnable);
        Log.i(TAG, "complete");
    }
    private class AnimatedTextView {
        private final TextView textView;

        public AnimatedTextView(TextView textView) {this.textView = textView;}
        public String getText() {return textView.getText().toString();}
        public void setText(String text) {textView.setText(text);}
    }

}
