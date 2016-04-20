package com.projects.kquicho.uwatm8;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private View mProgressBar;
    private View mContainer;


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
        mContainer = view.findViewById(R.id.container);
        mProgressBar = view.findViewById(R.id.pbLoading);
        mCoursesParser.setParseType(CourseParser.ParseType.COURSE_DETAILS.ordinal());
        mUrl = UWOpenDataAPI.buildURL(String.format(mCoursesParser.getEndPoint(), mSubject, mCatalogNumber));

        mProgressBar.setVisibility(View.VISIBLE);
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
                mProgressBar.setVisibility(View.GONE);
                mContainer.setVisibility(View.VISIBLE);
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
                        termsOffered.setVisibility(View.GONE);
                        mView.findViewById(R.id.title_terms_offered).setVisibility(View.GONE);
                        mView.findViewById(R.id.separator_terms_offered).setVisibility(View.GONE);
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
            }
        };
        handler.post(runnable);
        Log.i(TAG, "complete");
    }
}
