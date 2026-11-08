import csv
import math
from pathlib import Path


CSV_PATH = Path("results/tables/benchmark.csv")
PLOT_DIR = Path("results/plots")
WIDTH = 960
HEIGHT = 560
LEFT = 100
RIGHT = 910
TOP = 65
BOTTOM = 430


def read_rows():
    with CSV_PATH.open(newline="") as source:
        return list(csv.DictReader(source))


def make_plot(rows, task, x_label, file_name, series_definitions):
    selected = [row for row in rows if row["task"] == task]
    x_values = sorted({int(row["input_size"]) for row in selected})
    series = []
    for algorithm, test_case, label, color in series_definitions:
        points = [row for row in selected
                  if row["algorithm"] == algorithm
                  and (test_case is None or row["case"] == test_case)]
        if points:
            series.append((label, color, sorted(points, key=lambda row: int(row["input_size"]))))

    times = [float(row["average_time_ns"]) for _, _, points in series for row in points]
    minimum = min(times)
    maximum = max(times)
    low_power = math.floor(math.log10(minimum))
    high_power = math.ceil(math.log10(maximum))
    if low_power == high_power:
        high_power += 1
    low = 10 ** low_power
    high = 10 ** high_power

    def x_position(value):
        if len(x_values) == 1:
            return (LEFT + RIGHT) / 2
        plot_left = LEFT + 18
        plot_right = RIGHT - 18
        fraction = (value - x_values[0]) / (x_values[-1] - x_values[0])
        return plot_left + fraction * (plot_right - plot_left)

    def y_position(value):
        fraction = (math.log10(value) - math.log10(low)) / (math.log10(high) - math.log10(low))
        return BOTTOM - fraction * (BOTTOM - TOP)

    parts = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{WIDTH}" height="{HEIGHT}" viewBox="0 0 {WIDTH} {HEIGHT}">',
        f'<rect x="0" y="0" width="{WIDTH}" height="{HEIGHT}" fill="#ffffff"/>',
        '<style>text{font-family:Arial,sans-serif;fill:#222}.small{font-size:13px}.label{font-size:15px}.title{font-size:21px;font-weight:bold}</style>',
        f'<text x="{WIDTH / 2}" y="30" text-anchor="middle" class="title">Execution time vs. {x_label}</text>',
    ]
    for exponent in range(low_power, high_power + 1):
        tick = 10 ** exponent
        y = y_position(tick)
        parts.append(f'<line x1="{LEFT}" y1="{y:.1f}" x2="{RIGHT}" y2="{y:.1f}" stroke="#ddd"/>')
        parts.append(f'<text x="{LEFT - 10}" y="{y + 4:.1f}" text-anchor="end" class="small">{tick:g}</text>')
    parts.extend([
        f'<line x1="{LEFT}" y1="{TOP}" x2="{LEFT}" y2="{BOTTOM}" stroke="#333"/>',
        f'<line x1="{LEFT}" y1="{BOTTOM}" x2="{RIGHT}" y2="{BOTTOM}" stroke="#333"/>',
        f'<text x="22" y="{(TOP + BOTTOM) / 2}" text-anchor="middle" transform="rotate(-90 22 {(TOP + BOTTOM) / 2})" class="label">Average time (ns, log scale)</text>',
        f'<text x="{(LEFT + RIGHT) / 2}" y="{HEIGHT - 18}" text-anchor="middle" class="label">{x_label}</text>',
    ])
    for value in x_values:
        x = x_position(value)
        parts.append(f'<text x="{x:.1f}" y="{BOTTOM + 22}" text-anchor="middle" class="small">{value:,}</text>')
    for label, color, points in series:
        coordinates = " ".join(
            f'{x_position(int(row["input_size"])):.1f},{y_position(float(row["average_time_ns"])):.1f}'
            for row in points
        )
        parts.append(f'<polyline points="{coordinates}" fill="none" stroke="{color}" stroke-width="3"/>')
        for row in points:
            x = x_position(int(row["input_size"]))
            y = y_position(float(row["average_time_ns"]))
            parts.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="5" fill="{color}"/>')
    legend_width = 220
    legend_columns = max(1, min(3, len(series)))
    for index, (label, color, _) in enumerate(series):
        column = index % legend_columns
        row_index = index // legend_columns
        x = 105 + column * legend_width
        y = HEIGHT - 58 + row_index * 24
        parts.append(f'<line x1="{x}" y1="{y}" x2="{x + 24}" y2="{y}" stroke="{color}" stroke-width="3"/>')
        parts.append(f'<text x="{x + 31}" y="{y + 4}" class="small">{label}</text>')
    parts.append('</svg>')
    PLOT_DIR.mkdir(parents=True, exist_ok=True)
    (PLOT_DIR / file_name).write_text("\n".join(parts))


def main():
    rows = read_rows()
    make_plot(rows, "dynamic_programming", "Input length", "dp_time.svg", [
        ("LCS", None, "LCS", "#2563eb"),
        ("EditDistance", None, "Edit distance", "#ea580c"),
    ])
    match_series = []
    colors = {"Naive": "#2563eb", "KMP": "#15803d", "RabinKarp": "#9333ea"}
    for case, suffix in [("present", "present"), ("absent", "absent")]:
        for algorithm in ["Naive", "KMP", "RabinKarp"]:
            match_series.append((algorithm, case, f"{algorithm} {suffix}", colors[algorithm]))
    make_plot(rows, "string_matching", "Text length", "matching_time.svg", match_series)
    make_plot(rows, "kasai", "String length", "kasai_time.svg", [
        ("Kasai", None, "Kasai", "#0891b2"),
    ])
    print(f"Plots saved in {PLOT_DIR}")


if __name__ == "__main__":
    main()
