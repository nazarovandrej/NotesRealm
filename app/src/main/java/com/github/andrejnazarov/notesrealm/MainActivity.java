package com.github.andrejnazarov.notesrealm;

import android.content.DialogInterface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.andrejnazarov.notesrealm.adapter.CustomPagerAdapter;
import com.github.andrejnazarov.notesrealm.manager.RealmManager;
import com.github.andrejnazarov.notesrealm.model.Category;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private static final int CATEGORY_NAME_MIN_LENGTH = 3;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;

    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    private CustomPagerAdapter mAdapter;

    private Realm mRealm;

    //region Activity LifeCycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initUI();
        mAdapter = new CustomPagerAdapter(getSupportFragmentManager());
        mRealm = new RealmManager(this).getRealm();
        fullAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                closeApp();
                return true;
            case R.id.new_category:
                addNewCategory();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        closeApp();
    }

    //endregion

    //region private methods

    private void initUI() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void addNewCategory() {
        LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.create_dialog, null);
        final EditText editText = (EditText) view.findViewById(R.id.input_category_edit_text);
        new AlertDialog.Builder(this)
                .setTitle(R.string.input_category_name)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editText.getText().length() >= CATEGORY_NAME_MIN_LENGTH) {
                            saveCategoryName(editText.getText().toString().toLowerCase());
                            fullAdapter();
                        } else {
                            Toast.makeText(getApplicationContext(), "Минимум " + CATEGORY_NAME_MIN_LENGTH + " символов", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setView(view)
                .show();
    }

    private void saveCategoryName(String categoryName) {
        boolean contains = false;
        for (Category category : mRealm.allObjects(Category.class)) {
            if (category.getCategoryName().equals(categoryName)) {
                contains = true;
            }
        }
        if (contains) {
            Toast.makeText(getApplicationContext(), categoryName + " уже содержится", Toast.LENGTH_LONG).show();
        } else {
            mRealm.beginTransaction();
            Category category = mRealm.createObject(Category.class);
            category.setCategoryName(categoryName);
            mRealm.commitTransaction();
        }
    }

    private void fullAdapter() {
        mAdapter.clear();
        List<BasicFragment> fragments = new ArrayList<>();
        for (Category category : mRealm.allObjects(Category.class)) {
            fragments.add(BasicFragment.newInstance().setTitle(category.getCategoryName()));
        }
        mAdapter.addFragments(fragments);
        mViewPager.setAdapter(mAdapter);
        setTabsLongClickListener();
    }

    private void setTabsLongClickListener() {
        LinearLayout tabStrip = (LinearLayout) mTabLayout.getChildAt(0);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            final int index = i;
            tabStrip.getChildAt(i).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    deleteTabAt(index);
                    return false;
                }
            });
        }
    }

    private void deleteTabAt(final int position) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_category)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCategoryAtPosition(position);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void deleteCategoryAtPosition(final int position) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Category> rows = realm.where(Category.class)
                        .equalTo("categoryName", mRealm.allObjects(Category.class)
                                .get(position).getCategoryName()).findAll();
                rows.clear();
            }
        });
        fullAdapter();
    }

    private void closeApp() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit_app)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
