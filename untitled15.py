# -*- coding: utf-8 -*-
"""
Created on Sat Jun 13 21:33:40 2020

@author: molly
"""

import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as animation

thick=4
door_color="white"
exit_door_color="red"
building_color="black"

def draw_building():
    fig,ax = plt.subplots(figsize=(2.0,2.0))
    plt.xlim(-1,21)
    plt.ylim(-1,21)
    plt.xlabel("x")
    plt.ylabel("y")
    ax.grid()
    #rysowanie granic budynku
    ly1 = [0,0,20,20,0]
    lx1 = [0,20,20,0,0]
    plt.plot(lx1,ly1,color="black",zorder=5)
    #rysowanie 1.pokoju
    ly2 = [0,20] 
    lx2 = [5,5]
    plt.plot(lx2,ly2,color="black",zorder=5)
    #rysowanie 2 pokoju
    ly3 = [13,13] 
    lx3 = [5,20]
    plt.plot(lx3,ly3,color="black",zorder=5)
    #rysowanie 3 i 4 pokoju
    ly4 = [13,20] 
    lx4 = [12,12]
    plt.plot(lx4,ly4,color="black",zorder=5)
    #rysownaie drzwi
    #id=1
    ly5 = [0,0] 
    lx5 = [9,10]
    plt.plot(lx5,ly5,color=exit_door_color,linewidth=thick,zorder=5)
    #id=2
    ly6 = [9,10] 
    lx6 = [5,5]
    plt.plot(lx6,ly6,color=door_color,linewidth=thick,zorder=5)
    #id=3
    ly7 = [15,16] 
    lx7 = [12,12]
    plt.plot(lx7,ly7,color=door_color,linewidth=thick,zorder=5)
    #id=4
    ly8 = [13,13] 
    lx8 = [16,17]
    plt.plot(lx8,ly8,color=door_color,linewidth=thick,zorder=5)
    #rysowanie utrudnień
    x = [3,3,3,3,3]
    y = [3,4,5,6,7]
    points_whole_ax = 2 * 0.8 * 72    # 1 point = dpi / 72 pixels
    radius = 0.1
    points_radius = 2 * radius / 1.0 * points_whole_ax
    ax.scatter(x, y, s=points_radius**2)
    #ax.axis('off')
    

draw_building()