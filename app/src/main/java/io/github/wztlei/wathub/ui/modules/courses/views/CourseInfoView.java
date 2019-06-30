package io.github.wztlei.wathub.ui.modules.courses.views;

import android.content.Context;
import android.widget.TextView;

import com.deange.uwaterlooapi.model.courses.CourseInfo;

import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.model.CombinedCourseInfo;

import butterknife.BindView;

public class CourseInfoView extends BaseCourseView {

    @BindView(R.id.course_info_title)
    TextView mTitle;
    @BindView(R.id.course_info_description)
    TextView mDescription;

    public CourseInfoView(final Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_course_info;
    }

    @Override
    public void bind(final CombinedCourseInfo info) {
        final CourseInfo courseInfo = info.getCourseInfo();

        mTitle.setText(courseInfo.getTitle());
        mDescription.setText(courseInfo.getDescription());
    }
}
