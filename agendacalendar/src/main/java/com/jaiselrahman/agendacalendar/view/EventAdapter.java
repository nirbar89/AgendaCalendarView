package com.jaiselrahman.agendacalendar.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jaiselrahman.agendacalendar.R;
import com.jaiselrahman.agendacalendar.model.BaseEvent;
import com.jaiselrahman.agendacalendar.util.DateUtils;
import com.jaiselrahman.agendacalendar.util.EventCache;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.format.TextStyle;
import org.threeten.bp.temporal.ChronoField;

import java.util.List;

import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderAdapter;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class EventAdapter<E extends BaseEvent, T extends List<E>>
        extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private static final int EVENT = 0;
    private static final int EMPTY_EVENT = 1;
    private static final BaseEvent.Empty key = new BaseEvent.Empty(null);

    private final StickyHeaderAdapter<EventAdapter.HeaderViewHolder> eventStickyHeader =
            new StickyHeaderAdapter<EventAdapter.HeaderViewHolder>() {

                @Override
                final public long getHeaderId(int position) {
                    LocalDateTime date = eventList.get(position).getTime();
                    return date.getYear() * 1000 + date.getDayOfYear();
                }

                @Override
                final public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item_header, parent, false);
                    return new HeaderViewHolder(v);
                }

                @Override
                public void onBindHeaderViewHolder(HeaderViewHolder headerViewHolder, int position) {
                    headerViewHolder.bind(eventList.get(position));
                }
            };

    private OnEventClickListener<E> onEventClickListener;

    private EventList<E, T> eventList;

    public EventAdapter(EventList<E, T> eventList) {
        this.eventList = eventList;
        this.eventList.setAdapter(this);
    }

    private EventCache.Loader eventLoader = time -> {
        //noinspection unchecked
        return (List<BaseEvent>) eventList.getEvents(time);
    };

    final public void setEvents(T events) {
        eventList.setEvents(events);
    }

    public E getEvent(int position) {
        return eventList.get(position);
    }

    @NonNull
    @Override
    final public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == EVENT) {
            EventViewHolder holder = createEventViewHolder(parent);
            //noinspection unchecked
            holder.setOnEventClickListener(onEventClickListener);
            return holder;
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_empty_item, parent, false);
            return new EmptyEventHolder(v);
        }
    }

    public abstract EventViewHolder<E> createEventViewHolder(ViewGroup viewGroup);

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        BaseEvent event = eventList.get(position);
        //noinspection unchecked
        holder.bind(event);
        holder.event = event;
    }

    @Override
    public int getItemViewType(int position) {
        if (eventList.get(position) instanceof BaseEvent.Empty) {
            return EMPTY_EVENT;
        }
        return EVENT;
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public int getAdapterPosition(LocalDate localDate) {
        key.setTime(localDate.atStartOfDay());
        return eventList.possibleIndexOf(key);
    }

    public List<E> getEventsOn(LocalDate localDate) {
        //noinspection unchecked
        return (List<E>) EventCache.getEvents(localDate, eventLoader);
    }

    public void setOnEventClickListener(OnEventClickListener<E> onEventClickListener) {
        this.onEventClickListener = onEventClickListener;
    }

    void setOnEventSetListener(EventList.OnEventSetListener onEventSetListener) {
        eventList.setOnEventSetListener(onEventSetListener);
    }

    StickyHeaderAdapter<EventAdapter.HeaderViewHolder> getEventStickyHeader() {
        return eventStickyHeader;
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView day, date;
        private LinearLayout dateContainer;
        private DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
                .appendText(ChronoField.DAY_OF_WEEK, TextStyle.SHORT)
                .toFormatter();

        HeaderViewHolder(@NonNull View v) {
            super(v);
            day = v.findViewById(R.id.day);
            date = v.findViewById(R.id.date);
            dateContainer = v.findViewById(R.id.dateContainer);

        }

        void bind(BaseEvent event) {
            dateContainer.setLayoutDirection(View.LAYOUT_DIRECTION_LOCALE);
            date.setText(String.valueOf(event.getTime().getDayOfMonth()));
            day.setText(dateFormatter.format(event.getTime()));
            if (DateUtils.isToday(event.getTime())) {
                itemView.setBackgroundResource(R.drawable.event_current_item_header);
            }
        }
    }

    private static class EmptyEventHolder extends EventViewHolder<BaseEvent.Empty> {
        private TextView title;

        EmptyEventHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
        }

        public void bind(BaseEvent.Empty emptyEvent) {
            title.setText(emptyEvent.getTitle());
        }
    }

    public abstract static class EventViewHolder<E extends BaseEvent> extends RecyclerView.ViewHolder {
        private E event;

        public EventViewHolder(View v) {
            super(v);
        }

        private void setOnEventClickListener(OnEventClickListener<E> onEventClickListener) {
            itemView.setOnClickListener(v1 -> onEventClickListener.onEventClick(event));
        }

        public abstract void bind(E event);
    }

    public interface OnEventClickListener<T extends BaseEvent> {
        void onEventClick(T event);
    }
}
