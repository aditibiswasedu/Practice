import { supabase } from './supabaseClient.js';

const projectModal = document.getElementById("projectModal");
const projectForm = document.getElementById("projectForm");
const projectRows = document.getElementById("projectRows");
const toast = document.getElementById("toast");

function showToast(message) {
  toast.textContent = message;
  toast.classList.add("show");
  window.clearTimeout(showToast.timer);
  showToast.timer = window.setTimeout(() => toast.classList.remove("show"), 2600);
}

function openModal() {
  projectModal.hidden = false;
  document.getElementById("projectName").focus();
}

function closeModal() {
  projectModal.hidden = true;
  projectForm.reset();
}

document.getElementById("newProjectBtn").addEventListener("click", openModal);
document.getElementById("closeModal").addEventListener("click", closeModal);
projectModal.addEventListener("click", (event) => {
  if (event.target === projectModal) closeModal();
});
document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") closeModal();
});

function escapeHtml(text) {
  if (!text) return "";
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}

function renderProjectRow(project, prepend = false) {
  const name = project.name || "Untitled";
  const category = project.category || "General";
  const deadline = project.deadline;
  const progress = Number.isInteger(project.progress) ? project.progress : 0;
  const status = (project.status || "planning").toLowerCase();

  let formattedDate = "No date";
  if (deadline) {
    const date = new Date(`${deadline}T00:00:00`);
    formattedDate = isNaN(date.getTime())
      ? deadline
      : date.toLocaleDateString("en-US", { month: "short", day: "2-digit", year: "numeric" });
  }

  const firstLetter = name.charAt(0).toUpperCase();

  const statusClasses = {
    planning: "status-planning",
    "in progress": "status-progress",
    "in-progress": "status-progress",
    "in review": "status-review",
    "in-review": "status-review",
    completed: "status-complete"
  };

  const statusLabels = {
    planning: "Planning",
    "in progress": "In progress",
    "in-progress": "In progress",
    "in review": "In review",
    "in-review": "In review",
    completed: "Completed"
  };

  const badgeClass = statusClasses[status] || "status-planning";
  const badgeLabel = statusLabels[status] || "Planning";

  const symbolColors = ["purple", "peach", "mint", "sky", "sand"];
  const colorIndex = Math.abs(name.split("").reduce((acc, c) => acc + c.charCodeAt(0), 0)) % symbolColors.length;
  const color = symbolColors[colorIndex];

  const row = document.createElement("tr");
  if (project.id) row.dataset.id = project.id;
  row.dataset.search = `${name} ${category}`.toLowerCase();
  row.innerHTML = `
    <td>
      <div class="project-name">
        <span class="project-symbol ${color}">${firstLetter}</span>
        <span><strong>${escapeHtml(name)}</strong><small>${escapeHtml(category)}</small></span>
      </div>
    </td>
    <td>${formattedDate}</td>
    <td>
      <div class="progress-cell">
        <span>${progress}%</span>
        <i><b style="width:${progress}%"></b></i>
      </div>
    </td>
    <td><span class="status ${badgeClass}">${badgeLabel}</span></td>
    <td><button class="row-more" aria-label="Delete project" title="Click to delete">✕</button></td>
  `;

  if (prepend) {
    projectRows.prepend(row);
  } else {
    projectRows.appendChild(row);
  }
}

// Fetch all projects from Supabase
async function fetchProjects() {
  try {
    const { data, error } = await supabase
      .from("projects")
      .select("*")
      .order("created_at", { ascending: false });

    if (error) {
      console.error("Supabase fetch error:", error);
      showToast("Connected, but could not load projects: " + error.message);
      return;
    }

    if (data && data.length > 0) {
      projectRows.innerHTML = "";
      data.forEach((project) => renderProjectRow(project));
      updateStats(data);
    }
  } catch (err) {
    console.error("Failed to fetch projects from Supabase:", err);
  }
}

// Fetch tasks from Supabase to update live counters
async function fetchTasks() {
  try {
    const { data: tasks, error } = await supabase.from("tasks").select("*");
    if (!error && tasks) {
      const activeCount = tasks.filter((t) => t.status === "PENDING" || t.status === "IN_PROGRESS").length;
      const completedCount = tasks.filter((t) => t.status === "COMPLETED").length;

      const activeEl = document.querySelector(".stat-card:nth-child(2) .stat-value");
      const completedEl = document.querySelector(".stat-card:nth-child(3) .stat-value");
      if (activeEl) activeEl.textContent = activeCount;
      if (completedEl) completedEl.textContent = completedCount;
    }
  } catch (err) {
    console.error("Failed to fetch tasks:", err);
  }
}

function updateStats(projects) {
  if (!projects || projects.length === 0) return;
  const totalProjectsEl = document.querySelector(".stat-card:nth-child(1) .stat-value");
  const overallProgressEl = document.querySelector(".stat-card:nth-child(4) .stat-value");

  if (totalProjectsEl) {
    totalProjectsEl.textContent = projects.length;
  }
  if (overallProgressEl) {
    const totalProg = projects.reduce((acc, p) => acc + (p.progress || 0), 0);
    const avgProg = Math.round(totalProg / projects.length);
    overallProgressEl.textContent = `${avgProg}%`;
  }
}

// Handle Form Submission -> Insert into Supabase
projectForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const name = document.getElementById("projectName").value.trim();
  const category = document.getElementById("projectCategory").value.trim();
  const deadlineValue = document.getElementById("projectDeadline").value;
  if (!name || !category || !deadlineValue) return;

  const submitBtn = projectForm.querySelector('button[type="submit"]');
  submitBtn.disabled = true;
  submitBtn.textContent = "Saving to Supabase...";

  try {
    const newProject = {
      name,
      category,
      deadline: deadlineValue,
      progress: 0,
      status: "planning"
    };

    const { data, error } = await supabase
      .from("projects")
      .insert([newProject])
      .select()
      .single();

    if (error) {
      console.error("Supabase insert error:", error);
      showToast("Error saving to Supabase: " + error.message);
    } else {
      renderProjectRow(data, true);
      closeModal();
      showToast(`"${name}" was saved successfully to Supabase!`);
      document.getElementById("emptyState").hidden = true;
      fetchProjects();
    }
  } catch (err) {
    console.error("Unexpected error saving project:", err);
    showToast("Unexpected error occurred while saving.");
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = "Create project";
  }
});

// Delete handler
projectRows.addEventListener("click", async (e) => {
  const moreBtn = e.target.closest(".row-more");
  if (moreBtn) {
    const row = moreBtn.closest("tr");
    const id = row.dataset.id;
    const projectName = row.querySelector(".project-name strong")?.textContent || "this project";
    if (id && confirm(`Delete "${projectName}" from Supabase?`)) {
      const { error } = await supabase.from("projects").delete().eq("id", id);
      if (error) {
        showToast("Error deleting: " + error.message);
      } else {
        row.remove();
        showToast(`"${projectName}" was deleted from Supabase.`);
        fetchProjects();
      }
    }
  }
});

// Search filter
document.getElementById("searchInput").addEventListener("input", (event) => {
  const query = event.target.value.trim().toLowerCase();
  let visibleCount = 0;
  [...projectRows.querySelectorAll("tr")].forEach((row) => {
    const match = !query || row.dataset.search.includes(query);
    row.hidden = !match;
    if (match) visibleCount++;
  });
  document.getElementById("emptyState").hidden = visibleCount !== 0;
});

// Navigation actions
document.querySelectorAll(".nav-item").forEach((item) => {
  item.addEventListener("click", () => {
    document.querySelectorAll(".nav-item").forEach((nav) => nav.classList.remove("active"));
    item.classList.add("active");
    document.getElementById("currentPage").textContent = item.dataset.page;
    if (item.dataset.page !== "Dashboard") {
      showToast(`${item.dataset.page} section selected.`);
    }
  });
});

document.getElementById("viewAllBtn").addEventListener("click", () => showToast("Showing all workspace projects."));
document.getElementById("activityBtn").addEventListener("click", () => showToast("Live activity feed synced."));
document.getElementById("upgradeBtn").addEventListener("click", () => showToast("Upgrade options are available in billing."));
document.getElementById("dateButton").addEventListener("click", () => showToast("Viewing active workspace cycle."));

// Load data on startup
fetchProjects();
fetchTasks();
