package com.route.todoappc42.ui.screens.main.fragments.todo_list


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.DaySize
import com.kizitonwose.calendar.view.WeekDayBinder
import com.kizitonwose.calendar.view.WeekHeaderFooterBinder
import com.route.todoappc42.LocationActivity
import com.route.todoappc42.R
import com.route.todoappc42.database.MyDatabase
import com.route.todoappc42.databinding.FragmentTodoListBinding
import com.route.todoappc42.ui.model.Todo
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

class TodoListFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentTodoListBinding
    var adapter = TodosAdapter(emptyList())
    var selectedDate = WeekDay(date = LocalDate.now(), position = WeekDayPosition.InDate)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTodosRecycler()
        refreshTodosList()
        initCalendarView()
    }

    private fun initCalendarView() {
        binding.calendarView.dayBinder = object : WeekDayBinder<DayViewHolder> {
            override fun bind(container: DayViewHolder, data: WeekDay) {
                if (selectedDate.date.compareTo(data.date) == 0) {
                    Log.e("initCalendarView", "selectedDate.compareTo(data.date) == 0")
                    container.view.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.blue)
                } else {
                    container.view.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.white)
                }
                container.day.text = data.date.dayOfMonth.toString()
                container.name.text = data.date.dayOfWeek.name
                container.view.setOnClickListener {
                    var tempDate = selectedDate.copy()
                    selectedDate = data
                    binding.calendarView.notifyDayChanged(tempDate)
                    binding.calendarView.notifyDayChanged(data)
                    refreshTodosList()
                }
            }

            // Called only when a new container is needed.
            override fun create(view: View) = DayViewHolder(view)
        }
        binding.calendarView.daySize = DaySize.FreeForm
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12) // Adjust as needed
        val endMonth = currentMonth.plusMonths(12) // Adjust as needed
        val firstDayOfWeek = firstDayOfWeekFromLocale() // Available from the library
        binding.calendarView.setup(
            startMonth.atStartOfMonth(),
            endMonth.atStartOfMonth(),
            firstDayOfWeek
        )
        binding.calendarView.scrollToWeek(LocalDate.now())
        binding.calendarView.weekHeaderBinder = object : WeekHeaderFooterBinder<WeekViewHolder> {
            override fun bind(container: WeekViewHolder, data: Week) {
                container.week.text = "${data.days[0].date.month.name} - ${data.days[0].date.year}"
            }

            override fun create(view: View): WeekViewHolder = WeekViewHolder(view)

        }

    }

    fun refreshTodosList() {
        var todos = MyDatabase.getInstance().getTodoDao().getAllTodos()
        todos = todos.filter {
            val todoDate = Instant.ofEpochMilli(it.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            return@filter todoDate.year == selectedDate.date.year
                    && todoDate.month == selectedDate.date.month
                    && todoDate.dayOfMonth == selectedDate.date.dayOfMonth
        }
        adapter.submitList(todos)
    }

    private fun initTodosRecycler() {
        adapter.itemClickListener = object : TodosAdapter.ItemClickListener {
            override fun onItemClick(todo: Todo) {
                val todos = MyDatabase.getInstance().getTodoDao().getTodoById(todo.id)
                Log.e("initTodosRecycler", "todo name = ${todos.first().title}")
            }

            override fun onDoneClick(todo: Todo) {
            }

            override fun onDeleteClick(todo: Todo) {
                MyDatabase.getInstance().getTodoDao().deleteTodo(todo)
                refreshTodosList()
            }

        }
        binding.todosRecycler.adapter = adapter
    }

}
