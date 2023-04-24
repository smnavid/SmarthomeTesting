import os
import mysql.connector
import matplotlib.pyplot as plt

cnx = mysql.connector.connect(user='root', password='tmp')

db = cnx.cursor()

# DATE_FORMAT(create_time, '%M %d %Y')
# Execute a query
def execute_query():
    # Gets daily electricity use by experimentation group
    query = ("SELECT group_experiment, HOUR(create_time), SUM(minutes_lights_on) "
            "FROM TartanHome.Home "
            "WHERE `minutes_lights_on` IS NOT NULL AND `group_experiment` IS NOT NULL "
            "GROUP BY HOUR(create_time), group_experiment "
            )
    db.execute(query)
    results = db.fetchall()
    print(results)
    return results

# Preprocess data
def preprocess_data(raw_data):
    # List of tuples, i.e., (group, date, minutes)
    prep = list(map(lambda t:(t[0], t[1], float(t[2]) / (60 * 1000)), raw_data))
    result = dict()
    print()
    for e in prep:
        if e[0] in result:
            result[e[0]][0].append(e[1])
            result[e[0]][1].append(e[2])
            result[e[0]][2].append(e[3])
        else:
            result[e[0]] = [[ e[1] ], [ e[2] ], [ e[3] ]]
    # return dict((group, (day, )) for (group, day, month, minutes) in prep)
    return result

# Generate charts
def generate_charts(good_data):
    # List of tuples, i.e., (group, day, month, minutes)
    plt.plot(good_data['1'][0], good_data['1'][1], label='Group 1', color='skyblue', linewidth=4)
    plt.plot(good_data['2'][0], good_data['2'][1], label='Group 2', color='orange', linewidth=4)
    plt.plot(good_data['3'][0], good_data['3'][1], label='Group 3', color='green', linewidth=4)
    plt.legend()
    plt.show()

raw_data = execute_query()
print(raw_data)
good_data = preprocess_data(raw_data)
generate_charts(good_data)

db.close()
cnx.close()
