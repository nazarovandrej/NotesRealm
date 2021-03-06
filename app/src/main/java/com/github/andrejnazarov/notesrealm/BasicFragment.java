package com.github.andrejnazarov.notesrealm;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.andrejnazarov.notesrealm.adapter.NoteAdapter;
import com.github.andrejnazarov.notesrealm.manager.RealmManager;
import com.github.andrejnazarov.notesrealm.model.Category;
import com.github.andrejnazarov.notesrealm.model.Note;
import com.github.andrejnazarov.notesrealm.ui.CreateNoteActivity;
import com.github.andrejnazarov.notesrealm.ui.NoteDetailActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmList;

import static android.app.Activity.RESULT_OK;

/**
 * @author Nazarov on 09.07.17.
 */

public class BasicFragment extends Fragment implements NoteAdapter.NoteClickListener {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.add_note_button)
    FloatingActionButton mAddNoteButton;

    private Unbinder mUnbinder;
    private NoteAdapter mAdapter;

    private Realm mRealm;
    private String mTitle;

    public static BasicFragment newInstance() {
        return new BasicFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = new RealmManager(getContext()).getRealm();
        mAdapter = new NoteAdapter(this, getNotesRealm());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_basic, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView.setAdapter(mAdapter);
        mAddNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, getString(R.string.create_note_question), Snackbar.LENGTH_LONG)
                        .setAction(R.string.yes, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivityForResult(CreateNoteActivity.createExplicitIntent(getContext(), mTitle), 1);
                            }
                        }).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onClick(int position) {
        Note note = getNotesRealm().get(position);
        startActivityForResult(NoteDetailActivity.createExplicitIntent(getContext(),
                mTitle,
                note.getTitle(),
                note.getId(),
                note.getBody()), 1);
    }

    public String getTitle() {
        return mTitle;
    }

    public BasicFragment setTitle(String title) {
        mTitle = title;
        return this;
    }

    private RealmList<Note> getNotesRealm() {
        RealmList<Note> noteList = null;
        for (Category category : mRealm.allObjects(Category.class)) {
            if (category.getCategoryName().equals(mTitle)) {
                noteList = category.getNotes();
                break;
            }
        }
        return noteList;
    }
}
