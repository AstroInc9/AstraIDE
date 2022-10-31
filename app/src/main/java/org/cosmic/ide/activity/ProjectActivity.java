package org.cosmic.ide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.WorkerThread;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.pedrovgs.lynx.LynxConfig;
import com.github.pedrovgs.lynx.LynxActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.materialswitch.MaterialSwitch;

import org.cosmic.ide.R;
import org.cosmic.ide.activity.adapter.ProjectAdapter;
import org.cosmic.ide.common.util.CoroutineUtil;
import org.cosmic.ide.databinding.ActivityProjectBinding;
import org.cosmic.ide.project.Project;
import org.cosmic.ide.project.JavaProject;
import org.cosmic.ide.project.KotlinProject;
import org.cosmic.ide.util.Constants;
import org.cosmic.ide.util.UiUtilsKt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ProjectActivity extends BaseActivity {

    public interface OnProjectCreatedListener {
        void onProjectCreated(Project project);
    }

    private ProjectAdapter projectAdapter;
    private ActivityProjectBinding binding;

    private BottomSheetDialog createNewProjectDialog;
    private BottomSheetDialog deleteProjectDialog;

    private OnProjectCreatedListener mListener;

    public void setOnProjectCreatedListener(OnProjectCreatedListener listener) {
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        buildCreateNewProjectDialog();
        buildDeleteProjectDialog();

        setSupportActionBar(binding.toolbar);

        projectAdapter = new ProjectAdapter();
        binding.projectRecycler.setAdapter(projectAdapter);
        binding.projectRecycler.setLayoutManager(new LinearLayoutManager(this));
        projectAdapter.setOnProjectSelectedListener(this::openProject);
        projectAdapter.setOnProjectLongClickedListener(this::deleteProject);
        setOnProjectCreatedListener(this::openProject);

        UiUtilsKt.addSystemWindowInsetToMargin(binding.fab, false, false, false, true);
        UiUtilsKt.addSystemWindowInsetToPadding(binding.appbar, false, true, false, false);
        UiUtilsKt.addSystemWindowInsetToPadding(binding.projectRecycler, false, false, false, true);

        binding.refreshLayout.setOnRefreshListener(
                () -> {
                    loadProjects();
                    binding.refreshLayout.setRefreshing(false);
                });
        binding.fab.setOnClickListener(v -> showCreateNewProjectDialog());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.projects_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.logcat:
                startActivity(LynxActivity.getIntent(this, new LynxConfig()));
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProjects();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (createNewProjectDialog.isShowing()) {
            createNewProjectDialog.dismiss();
        }
        if (deleteProjectDialog.isShowing()) {
            deleteProjectDialog.dismiss();
        }
    }

    private void buildCreateNewProjectDialog() {
        createNewProjectDialog = new BottomSheetDialog(this);
        createNewProjectDialog.setContentView(R.layout.create_new_project_dialog);
    }

    private void buildDeleteProjectDialog() {
        deleteProjectDialog = new BottomSheetDialog(this);
        deleteProjectDialog.setContentView(R.layout.delete_dialog);
    }

    @WorkerThread
    private void showCreateNewProjectDialog() {
        if (!createNewProjectDialog.isShowing()) {
            createNewProjectDialog.show();
            EditText input = createNewProjectDialog.findViewById(android.R.id.text1);
            Button cancelBtn = createNewProjectDialog.findViewById(android.R.id.button2);
            Button createBtn = createNewProjectDialog.findViewById(android.R.id.button1);
            MaterialSwitch kotlinTemplate = createNewProjectDialog.findViewById(R.id.use_kotlin_template);
            cancelBtn.setOnClickListener(v -> createNewProjectDialog.dismiss());
            createBtn.setOnClickListener(
                    v -> {
                        var projectName = input.getText().toString().trim().replace("..", "");
                        if (projectName.isEmpty()) {
                            return;
                        }
                        boolean useKotlinTemplate = kotlinTemplate.isChecked();
                        try {
                            var project = useKotlinTemplate ? KotlinProject.newProject(projectName) : JavaProject.newProject(projectName);
                            if (mListener != null) {
                                runOnUiThread(
                                        () -> {
                                            if (createNewProjectDialog.isShowing())
                                                createNewProjectDialog.dismiss();
                                            mListener.onProjectCreated(project);
                                        });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            input.setText("");
        }
    }

    @WorkerThread
    private void showDeleteProjectDialog(JavaProject project) {
        if (!deleteProjectDialog.isShowing()) {
            deleteProjectDialog.show();
            TextView message = deleteProjectDialog.findViewById(android.R.id.message);
            Button cancelBtn = deleteProjectDialog.findViewById(android.R.id.button2);
            Button deleteBtn = deleteProjectDialog.findViewById(android.R.id.button1);
            cancelBtn.setOnClickListener(v -> deleteProjectDialog.dismiss());
            message.setText(getString(R.string.project_delete_warning, project.getProjectName()));
            deleteBtn.setOnClickListener(
                    v -> {
                        runOnUiThread(
                                () -> {
                                    if (deleteProjectDialog.isShowing())
                                        deleteProjectDialog.dismiss();
                                    project.delete();
                                    loadProjects();
                                });
                    });
        }
    }

    private void openProject(Project project) {
        var projectPath = project.getProjectDirPath();
        var intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constants.PROJECT_PATH, projectPath);
        startActivity(intent);
    }

    private boolean deleteProject(JavaProject project) {
        showDeleteProjectDialog(project);
        return true;
    }

    @WorkerThread
    private void loadProjects() {
        CoroutineUtil.inParallel(
                () -> {
                    var projectDir = new File(JavaProject.getRootDirPath());
                    var directories = projectDir.listFiles(File::isDirectory);
                    var projects = new ArrayList<JavaProject>();
                    if (directories != null) {
                        Arrays.sort(directories, Comparator.comparingLong(File::lastModified));
                        for (var directory : directories) {
                            var project = new File(directory, "src");
                            if (project.exists()) {
                                var javaProject =
                                        new JavaProject(new File(directory.getAbsolutePath()));
                                projects.add(javaProject);
                            }
                        }
                    }
                    runOnUiThread(
                            () -> {
                                projectAdapter.submitList(projects);
                                toggleNullProject(projects);
                            });
                });
    }

    private void toggleNullProject(List<JavaProject> projects) {
        if (projects.size() == 0) {
            binding.projectRecycler.setVisibility(View.GONE);
            binding.emptyContainer.setVisibility(View.VISIBLE);
        } else {
            binding.projectRecycler.setVisibility(View.VISIBLE);
            binding.emptyContainer.setVisibility(View.GONE);
        }
    }
}
