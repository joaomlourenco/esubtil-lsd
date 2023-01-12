#!/usr/bin/python3
import os
import sys
import math
import numpy as np
import matplotlib.pyplot as plt

# SECTION Helper Funcs


def parse_file(path):
    parsedLines = []

    with open(path) as filec:
        for line in filec:
            if line.startswith("progress:"):
                tks = line.split(",")
                for idx, tk in enumerate(tks):
                    tks[idx] = float(tk.split(":")[1].strip())
                parsedLines.append((tks[1], tks[2]))

    return parsedLines


def add_tuples(first_log, second_log):
    new_log = []
    for idx, t in enumerate(first_log):
        a, b1, c1 = t
        _, b2, c2 = second_log[idx]
        new_log.append((a, (b1, b2), (c1, c2)))


def correlate_data(data):

    time_values = [*range(len(data[0]))]

    lsd_tpm_total_values = [0.0] * len(time_values)
    lsd_tpmc_values = [0.0] * len(time_values)

    psql_tpm_total_values = [0.0] * len(time_values)
    psql_tpmc_values = [0.0] * len(time_values)

    n = len(data)
    for i in range(0, n, 2):
        lsd_data = data[i]
        psql_data = data[i + 1]

        for idx, log in enumerate(lsd_data):
            if idx < len(time_values):
                tpm_total, tpm_c = log
                lsd_tpm_total_values[idx] += tpm_total
                lsd_tpmc_values[idx] += tpm_c

        for idx, log in enumerate(psql_data):
            if idx < len(time_values):
                tpm_total, tpm_c = log
                psql_tpm_total_values[idx] += tpm_total
                psql_tpmc_values[idx] += tpm_c

        lsd_tpm_total_values[:] = [x / n for x in lsd_tpm_total_values]
        lsd_tpmc_values[:] = [x / n for x in lsd_tpmc_values]

        psql_tpm_total_values[:] = [x / n for x in psql_tpm_total_values]
        psql_tpmc_values[:] = [x / n for x in psql_tpmc_values]

        time_axis = np.array(time_values)
        lsd_tpm_total_axis = np.array(lsd_tpm_total_values)
        lsd_tpmc_axis = np.array(lsd_tpmc_values)
        psql_tpm_total_axis = np.array(psql_tpm_total_values)
        psql_tpmc_axis = np.array(psql_tpmc_values)

    return time_axis, lsd_tpm_total_axis, lsd_tpmc_axis, psql_tpm_total_axis, psql_tpmc_axis


def parseData(path):
    files = os.listdir(path)
    files.sort()
    parsedFiles = []
    for filename in files:
        filepath = path + "/" + filename
        if os.path.isfile(filepath) and filepath.lower().endswith(('.log')):
            parsedFiles.append(parse_file(filepath))

    return correlate_data(parsedFiles)


def generatePlots(time_axis, lsd_tpm_total_axis, lsd_tpmc_axis, psql_tpm_total_axis, psql_tpmc_axis):

    plt.figure()
    plt.title("tpmTOTAL over Time")
    plt.xlabel("Time (s)")
    plt.ylabel("tpmTOTAL")
    plt.plot(time_axis, lsd_tpm_total_axis, label="LSD", color="purple")
    plt.plot(time_axis, psql_tpm_total_axis, label="PSQL", color="green")
    plt.legend()
    plt.savefig(sys.argv[1] + "tpm_total")
    plt.close()

    plt.figure()
    plt.title("tpmC over Time")
    plt.xlabel("Time (s)")
    plt.ylabel("tpmC")
    plt.plot(time_axis, lsd_tpmc_axis, label="LSD", color="purple")
    plt.plot(time_axis, psql_tpmc_axis, label="PSQL", color="green")
    plt.legend()
    plt.savefig(sys.argv[1] + "tpmc")
    plt.close()


# SECTION MAIN

time_axis, lsd_tpm_total_axis, lsd_tpmc_axis, psql_tpm_total_axis, psql_tpmc_axis = parseData(
    sys.argv[1])

generatePlots(time_axis, lsd_tpm_total_axis, lsd_tpmc_axis, psql_tpm_total_axis, psql_tpmc_axis)
