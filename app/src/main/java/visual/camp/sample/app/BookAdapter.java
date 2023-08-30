package visual.camp.sample.app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import visual.camp.sample.app.activity.NovelMainActivity;
import visual.camp.sample.app.activity.NovelReadActivity;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private List<Book> bookList;
    private Context context;

    public BookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.tvBookName.setText(book.getBookName());
        holder.tvAuthorName.setText(book.getAuthorName());

        // 设置点击监听器
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NovelReadActivity.class);
            // 可以传递额外的数据，例如书的ID或标题，到ReadingActivity
            intent.putExtra("BOOK_NAME", book.getBookName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookName;
        TextView tvAuthorName;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookName = itemView.findViewById(R.id.tvBookName);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
        }
    }
}

