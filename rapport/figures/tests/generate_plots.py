"""
Generate test-result plots for LudoHeritage rapport.
Output: rapport/figures/tests/*.png
"""
import os
import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.gridspec import GridSpec

# ── Theme colours (matching the LaTeX template) ──────────────────────────────
LUDO    = "#1F6F78"   # ludocolor  RGB(31,111,120)
ACCENT  = "#8A5A22"   # accentcolor RGB(138,90,34)
PASS_C  = "#2E8B57"   # sea-green  – passing tests
FAIL_C  = "#C0392B"   # crimson    – failing tests (none here)
LIGHT   = "#F0F6F7"   # very light ludo
GREY    = "#7F8C8D"

OUT = os.path.dirname(os.path.abspath(__file__)) if "__file__" in dir() else os.getcwd()

plt.rcParams.update({
    "font.family":    "DejaVu Sans",
    "font.size":      11,
    "axes.spines.top":    False,
    "axes.spines.right":  False,
    "figure.dpi":         150,
})

# ─────────────────────────────────────────────────────────────────────────────
# DATA
# ─────────────────────────────────────────────────────────────────────────────

unit_classes = {
    "AuthService\nTest":  9,
    "GameService\nTest": 17,
}
integ_classes = {
    "AuthController\nTest":  10,
    "GameController\nTest":  10,
    "QuizController\nTest":   4,
}
stress_classes = {
    "GameService\nStressTest": 3,
}

all_classes = {**unit_classes, **integ_classes, **stress_classes}
all_counts  = list(all_classes.values())
all_labels  = list(all_classes.keys())
# category colour per bar
bar_colors = (
    [LUDO]   * len(unit_classes) +
    [ACCENT] * len(integ_classes) +
    [GREY]   * len(stress_classes)
)

# Stress data (from actual test output)
stress_scenarios = [
    "getAllGames\n50 threads×100",
    "Mixed ops\n50 threads×60",
    "Search/filter\n30 threads×80",
]
total_calls = [5_000, 3_000, 2_400]
avg_ms      = [0.422, 0.122, 0.657]
p95_ms      = [2.171, 0.381, 2.583]
p99_ms      = [10.304, 1.939, 10.378]
error_rate  = [0.0,   0.0,   0.0  ]

# ─────────────────────────────────────────────────────────────────────────────
# FIGURE 1 – Test counts per class (bar chart)
# ─────────────────────────────────────────────────────────────────────────────
fig, ax = plt.subplots(figsize=(10, 5))
x = np.arange(len(all_labels))
bars = ax.bar(x, all_counts, color=bar_colors, width=0.6, zorder=3,
              edgecolor="white", linewidth=0.8)

for bar, val in zip(bars, all_counts):
    ax.text(bar.get_x() + bar.get_width() / 2, bar.get_height() + 0.3,
            str(val), ha="center", va="bottom", fontweight="bold", fontsize=12)

ax.set_xticks(x)
ax.set_xticklabels(all_labels, fontsize=10)
ax.set_ylabel("Nombre de tests", fontsize=12)
ax.set_title("Répartition des tests par classe", fontsize=14, fontweight="bold",
             color=LUDO, pad=12)
ax.set_ylim(0, max(all_counts) + 4)
ax.grid(axis="y", linestyle="--", alpha=0.4, zorder=0)
ax.yaxis.set_major_locator(plt.MaxNLocator(integer=True))

legend_patches = [
    mpatches.Patch(color=LUDO,   label="Tests unitaires"),
    mpatches.Patch(color=ACCENT, label="Tests d'intégration"),
    mpatches.Patch(color=GREY,   label="Tests de stress"),
]
ax.legend(handles=legend_patches, loc="upper right", framealpha=0.9)

# Annotate total
ax.annotate(f"Total : {sum(all_counts)} tests — 0 échec",
            xy=(0.5, 0.93), xycoords="axes fraction",
            ha="center", fontsize=11, color=PASS_C, fontweight="bold")

plt.tight_layout()
fig.savefig(os.path.join(OUT, "fig_test_counts.png"), bbox_inches="tight")
plt.close(fig)
print("✓ fig_test_counts.png")

# ─────────────────────────────────────────────────────────────────────────────
# FIGURE 2 – Overall pass/fail donut
# ─────────────────────────────────────────────────────────────────────────────
fig, ax = plt.subplots(figsize=(6, 6))
sizes  = [53, 0]   # passed, failed
labels = ["Passés (53)", "Échoués (0)"]
colors = [PASS_C, FAIL_C]
explode = (0.04, 0)

wedges, texts, autotexts = ax.pie(
    sizes, labels=labels, colors=colors, explode=explode,
    autopct=lambda p: f"{p:.0f} %" if p > 0 else "",
    startangle=90, wedgeprops=dict(width=0.5, edgecolor="white", linewidth=2),
    textprops=dict(fontsize=12)
)
autotexts[0].set_fontsize(16)
autotexts[0].set_fontweight("bold")
autotexts[0].set_color("white")

ax.text(0, 0, "53\ntests", ha="center", va="center",
        fontsize=18, fontweight="bold", color=LUDO)
ax.set_title("Résultats globaux des tests\n(Build Maven)", fontsize=14,
             fontweight="bold", color=LUDO, pad=18)

plt.tight_layout()
fig.savefig(os.path.join(OUT, "fig_test_donut.png"), bbox_inches="tight")
plt.close(fig)
print("✓ fig_test_donut.png")

# ─────────────────────────────────────────────────────────────────────────────
# FIGURE 3 – Stress test latency (grouped bars: avg / P95 / P99)
# ─────────────────────────────────────────────────────────────────────────────
fig, ax = plt.subplots(figsize=(10, 5.5))
x = np.arange(len(stress_scenarios))
w = 0.25

b1 = ax.bar(x - w,     avg_ms, w, label="Latence moyenne", color=LUDO,   zorder=3)
b2 = ax.bar(x,         p95_ms, w, label="P95",             color=ACCENT, zorder=3)
b3 = ax.bar(x + w,     p99_ms, w, label="P99",             color=GREY,   zorder=3)

for bars in (b1, b2, b3):
    for bar in bars:
        h = bar.get_height()
        ax.text(bar.get_x() + bar.get_width() / 2, h + 0.1,
                f"{h:.2f}", ha="center", va="bottom", fontsize=8.5)

ax.set_xticks(x)
ax.set_xticklabels(stress_scenarios, fontsize=10)
ax.set_ylabel("Latence (ms)", fontsize=12)
ax.set_title("Latences des tests de stress (ms)", fontsize=14,
             fontweight="bold", color=LUDO, pad=12)
ax.legend(fontsize=11, framealpha=0.9)
ax.grid(axis="y", linestyle="--", alpha=0.4, zorder=0)
ax.set_ylim(0, max(p99_ms) * 1.25)

plt.tight_layout()
fig.savefig(os.path.join(OUT, "fig_stress_latency.png"), bbox_inches="tight")
plt.close(fig)
print("✓ fig_stress_latency.png")

# ─────────────────────────────────────────────────────────────────────────────
# FIGURE 4 – Stress test throughput (calls / error rate)
# ─────────────────────────────────────────────────────────────────────────────
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))

# Left: total calls per scenario
short_labels = ["getAllGames\n50×100", "Mixed ops\n50×60", "Search/filter\n30×80"]
colors_s = [LUDO, ACCENT, GREY]
bars = ax1.bar(short_labels, total_calls, color=colors_s, zorder=3,
               edgecolor="white", linewidth=0.8)
for bar, val in zip(bars, total_calls):
    ax1.text(bar.get_x() + bar.get_width() / 2, bar.get_height() + 40,
             f"{val:,}", ha="center", va="bottom", fontweight="bold")
ax1.set_ylabel("Nombre d'appels", fontsize=12)
ax1.set_title("Volume de charge par scénario", fontsize=13,
              fontweight="bold", color=LUDO, pad=10)
ax1.set_ylim(0, max(total_calls) * 1.2)
ax1.grid(axis="y", linestyle="--", alpha=0.4, zorder=0)

# Right: error rate (all 0 % – show as horizontal line)
ax2.bar(short_labels, [1, 1, 1], color=[LIGHT]*3, zorder=2,
        edgecolor=LUDO, linewidth=1.2, label="Taux d'erreur possible")
ax2.bar(short_labels, error_rate, color=[FAIL_C]*3, zorder=3,
        label="Taux d'erreur réel")
ax2.axhline(0, color=PASS_C, linewidth=2, linestyle="--",
            label="Objectif : 0 %")
ax2.set_ylabel("Taux d'erreur (%)", fontsize=12)
ax2.set_title("Taux d'erreur sous charge concurrente", fontsize=13,
              fontweight="bold", color=LUDO, pad=10)
ax2.set_ylim(-0.1, 1.5)
ax2.legend(fontsize=9, framealpha=0.9)
ax2.grid(axis="y", linestyle="--", alpha=0.4, zorder=0)

# Annotate 0 %
for i, label in enumerate(short_labels):
    ax2.text(i, 0.08, "0 %", ha="center", fontsize=13,
             color=PASS_C, fontweight="bold")

plt.suptitle("Tests de stress — LudoHeritage", fontsize=15,
             fontweight="bold", color=LUDO, y=1.01)
plt.tight_layout()
fig.savefig(os.path.join(OUT, "fig_stress_throughput.png"), bbox_inches="tight")
plt.close(fig)
print("✓ fig_stress_throughput.png")

# ─────────────────────────────────────────────────────────────────────────────
# FIGURE 5 – Dashboard summary (2×2 grid)
# ─────────────────────────────────────────────────────────────────────────────
fig = plt.figure(figsize=(14, 10))
fig.suptitle("Tableau de bord des tests — LudoHeritage", fontsize=16,
             fontweight="bold", color=LUDO, y=0.98)
gs = GridSpec(2, 2, figure=fig, hspace=0.45, wspace=0.35)

# ── Top-left: test counts ────────────────────────────────────────────────────
ax_a = fig.add_subplot(gs[0, 0])
x = np.arange(len(all_labels))
bars_a = ax_a.bar(x, all_counts, color=bar_colors, zorder=3,
                  edgecolor="white", linewidth=0.8)
for bar, val in zip(bars_a, all_counts):
    ax_a.text(bar.get_x() + bar.get_width() / 2, bar.get_height() + 0.2,
              str(val), ha="center", va="bottom", fontweight="bold", fontsize=9)
ax_a.set_xticks(x)
ax_a.set_xticklabels(all_labels, fontsize=7.5)
ax_a.set_ylabel("Tests", fontsize=10)
ax_a.set_title("Tests par classe", fontsize=12, fontweight="bold", color=LUDO)
ax_a.set_ylim(0, max(all_counts) + 4)
ax_a.grid(axis="y", linestyle="--", alpha=0.3, zorder=0)
legend_patches = [
    mpatches.Patch(color=LUDO,   label="Unitaires"),
    mpatches.Patch(color=ACCENT, label="Intégration"),
    mpatches.Patch(color=GREY,   label="Stress"),
]
ax_a.legend(handles=legend_patches, fontsize=8, loc="upper right")

# ── Top-right: donut ─────────────────────────────────────────────────────────
ax_b = fig.add_subplot(gs[0, 1])
wedges_b, _, autotexts_b = ax_b.pie(
    [53, 0.001], colors=[PASS_C, FAIL_C], explode=(0.03, 0),
    autopct=lambda p: f"{p:.0f} %" if p > 1 else "",
    startangle=90, wedgeprops=dict(width=0.5, edgecolor="white", linewidth=1.5),
    textprops=dict(fontsize=10)
)
autotexts_b[0].set_fontweight("bold")
autotexts_b[0].set_color("white")
ax_b.text(0, 0, "53/53\npassés", ha="center", va="center",
          fontsize=13, fontweight="bold", color=LUDO)
ax_b.set_title("Taux de réussite global", fontsize=12,
               fontweight="bold", color=LUDO)

# ── Bottom-left: latency bars ────────────────────────────────────────────────
ax_c = fig.add_subplot(gs[1, 0])
x = np.arange(len(stress_scenarios))
w = 0.25
bc1 = ax_c.bar(x - w, avg_ms, w, label="Moy", color=LUDO,   zorder=3)
bc2 = ax_c.bar(x,     p95_ms, w, label="P95", color=ACCENT, zorder=3)
bc3 = ax_c.bar(x + w, p99_ms, w, label="P99", color=GREY,   zorder=3)
for bars in (bc1, bc2, bc3):
    for bar in bars:
        h = bar.get_height()
        ax_c.text(bar.get_x() + bar.get_width() / 2, h + 0.1,
                  f"{h:.2f}", ha="center", va="bottom", fontsize=7)
ax_c.set_xticks(x)
ax_c.set_xticklabels(["getAllGames", "Mixed ops", "Search/filter"], fontsize=9)
ax_c.set_ylabel("Latence (ms)", fontsize=10)
ax_c.set_title("Latences de stress (ms)", fontsize=12,
               fontweight="bold", color=LUDO)
ax_c.legend(fontsize=9)
ax_c.grid(axis="y", linestyle="--", alpha=0.3, zorder=0)
ax_c.set_ylim(0, max(p99_ms) * 1.3)

# ── Bottom-right: summary table ───────────────────────────────────────────────
ax_d = fig.add_subplot(gs[1, 1])
ax_d.axis("off")

table_data = [
    ["Scénario",            "Appels",  "Erreurs", "Moy (ms)", "P99 (ms)"],
    ["getAllGames (50×100)", "5 000",   "0 (0 %)", "0.422",    "10.30"],
    ["Mixed ops (50×60)",   "3 000",   "0 (0 %)", "0.122",    "1.94"],
    ["Search/filter (30×80)","2 400",  "0 (0 %)", "0.657",    "10.38"],
    ["TOTAL",               "10 400",  "0 (0 %)", "—",        "—"],
]

col_widths = [0.35, 0.15, 0.16, 0.17, 0.17]
table = ax_d.table(
    cellText=table_data[1:],
    colLabels=table_data[0],
    colWidths=col_widths,
    loc="center",
    cellLoc="center",
)
table.auto_set_font_size(False)
table.set_fontsize(9)
table.scale(1, 1.6)

for (row, col), cell in table.get_celld().items():
    if row == 0:
        cell.set_facecolor(LUDO)
        cell.set_text_props(color="white", fontweight="bold")
    elif row == len(table_data) - 1:
        cell.set_facecolor(LIGHT)
        cell.set_text_props(fontweight="bold")
    elif row % 2 == 0:
        cell.set_facecolor("#EAF3F4")
    else:
        cell.set_facecolor("white")
    cell.set_edgecolor("#BDC3C7")

ax_d.set_title("Résumé des tests de stress", fontsize=12,
               fontweight="bold", color=LUDO, pad=12)

plt.tight_layout()
fig.savefig(os.path.join(OUT, "fig_dashboard.png"), bbox_inches="tight", dpi=150)
plt.close(fig)
print("✓ fig_dashboard.png")

# ─────────────────────────────────────────────────────────────────────────────
# FIGURE 6 – Execution time per test class (horizontal bar)
# ─────────────────────────────────────────────────────────────────────────────
class_labels = [
    "AuthControllerTest",
    "AuthServiceTest",
    "GameControllerTest",
    "QuizControllerTest",
    "GameServiceTest",
    "GameServiceStressTest",
]
elapsed_s = [3.857, 0.279, 0.459, 0.357, 0.104, 0.207]
types     = ["Intégration","Unitaire","Intégration","Intégration","Unitaire","Stress"]
type_color = {
    "Unitaire":     LUDO,
    "Intégration":  ACCENT,
    "Stress":       GREY,
}
colors_h = [type_color[t] for t in types]

fig, ax = plt.subplots(figsize=(10, 5))
y = np.arange(len(class_labels))
hbars = ax.barh(y, elapsed_s, color=colors_h, zorder=3,
                edgecolor="white", linewidth=0.8, height=0.55)
for bar, val in zip(hbars, elapsed_s):
    ax.text(val + 0.03, bar.get_y() + bar.get_height() / 2,
            f"{val:.3f} s", va="center", fontsize=10)
ax.set_yticks(y)
ax.set_yticklabels(class_labels, fontsize=10)
ax.set_xlabel("Durée d'exécution (secondes)", fontsize=12)
ax.set_title("Durée d'exécution par classe de test", fontsize=14,
             fontweight="bold", color=LUDO, pad=12)
ax.set_xlim(0, max(elapsed_s) * 1.25)
ax.grid(axis="x", linestyle="--", alpha=0.4, zorder=0)

legend_patches = [mpatches.Patch(color=c, label=t)
                  for t, c in type_color.items()]
ax.legend(handles=legend_patches, loc="lower right", framealpha=0.9)

plt.tight_layout()
fig.savefig(os.path.join(OUT, "fig_exec_time.png"), bbox_inches="tight")
plt.close(fig)
print("✓ fig_exec_time.png")

print(f"\nAll plots saved to: {OUT}")
