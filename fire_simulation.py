# -*- coding: utf-8 -*-
"""
Created on Sat Jun 1 21:33:40 2020

@author: Maria Skarbek

"""

from matplotlib.backends.backend_tkagg import (
    FigureCanvasTkAgg, NavigationToolbar2Tk)
import matplotlib
from matplotlib.figure import Figure
from matplotlib import pyplot as plt
import numpy as np
from numpy import genfromtxt
from tkinter import *
from tkinter import messagebox
import os
import matplotlib.animation as animation
import matplotlib.ticker as ticker
import pandas as pd



#View
PROGRAM_NAME = ' SAG simulation '
PROGRAM_SIZE= "2000x2000"
COLOR_BLACK='#000000'
COLOR_SELEDINE='#a8e6cf'
COLOR_GREEN='#dcedc1'
FONT=("Helvetica", 9)
#SL
TITLE=("SAG Simulation")
thick=2
door_color="orangered"
exit_door_color="orangered"
building_color="black"

class View:
    
    def __init__(self, master):
        self.master = master
        self.master_params()
        self.init_gui()

    def master_params(self):
        self.master.title(PROGRAM_NAME)
        self.master.configure(bg=COLOR_GREEN)
        self.master.geometry(PROGRAM_SIZE)
         
    def close_window(self):
        self.master.quit()
        self.master.destroy()
        
    def create_building(self):
        self.fig,self.ax = plt.subplots(figsize=(10,10))
        plt.xlim(-1,22)
        plt.ylim(-1,22)
        plt.xlabel("X")
        plt.ylabel("Y")
        self.ax.grid()
        #rysowanie granic i scian budynku
        ly1 = [0,0,21,21,0]
        lx1 = [0,21,21,0,0]
        plt.plot(lx1,ly1,color=COLOR_BLACK,linewidth=thick,zorder=5)
        #sciany budynku
        sy1 = [1,20]
        sx1 = [1,1]
        plt.plot(sx1,sy1,color=COLOR_BLACK,linewidth=thick,zorder=5)
        sy2 = [1,1,20,20]
        sx2 = [1,5,5,1]
        plt.plot(sx2,sy2,color=COLOR_BLACK,linewidth=thick,zorder=5)      
        sy3 = [1,1]
        sx3 = [6,20]
        plt.plot(sx3,sy3,color=COLOR_BLACK,linewidth=thick,zorder=5)  
        sy4 = [1,13]
        sx4 = [5,5]
        plt.plot(sx4,sy4,color=COLOR_BLACK,linewidth=thick,zorder=5)  
        sy5 = [1,13]
        sx5 = [20,20]
        plt.plot(sx5,sy5,color=COLOR_BLACK,linewidth=thick,zorder=5)  
        sy6 = [13,13]
        sx6 = [6,20]
        plt.plot(sx6,sy6,color=COLOR_BLACK,linewidth=thick,zorder=5) 
        sy7 = [14,20]
        sx7 = [5,5]
        plt.plot(sx7,sy7,color=COLOR_BLACK,linewidth=thick,zorder=5) 
        sy8 = [1,13]
        sx8 = [6,6]
        plt.plot(sx8,sy8,color=COLOR_BLACK,linewidth=thick,zorder=5) 
        sy9 = [14,20]
        sx9 = [6,6]
        plt.plot(sx9,sy9,color=COLOR_BLACK,linewidth=thick,zorder=5) 
        sy10 = [14,14]
        sx10 = [6,12]
        plt.plot(sx10,sy10,color=COLOR_BLACK,linewidth=thick,zorder=5) 
        sy11 = [20,20]
        sx11 = [6,12]
        plt.plot(sx11,sy11,color=COLOR_BLACK,linewidth=thick,zorder=5) 
        sy12 = [14,14]
        sx12 = [13,20]
        plt.plot(sx12,sy12,color=COLOR_BLACK,linewidth=thick,zorder=5) 
        sy13 = [20,20]
        sx13 = [13,20]
        plt.plot(sx13,sy13,color=COLOR_BLACK,linewidth=thick,zorder=5) 
        sy14 = [14,20]
        sx14 = [13,13]
        plt.plot(sx14,sy14,color=COLOR_BLACK,linewidth=thick,zorder=5) 
        sy15 = [14,20]
        sx15 = [20,20]
        plt.plot(sx15,sy15,color=COLOR_BLACK,linewidth=thick,zorder=5) 
        ly4 = [14,20] 
        lx4 = [12,12]
        plt.plot(lx4,ly4,color=COLOR_BLACK,linewidth=thick,zorder=5)
        #rysownaie drzwi
        #id=1
        ly5 = [0,1,1,0,0] 
        lx5 = [9,9,10,10,9]
        plt.plot(lx5,ly5,color=exit_door_color,linewidth=thick,zorder=5)
        #id=2
        ly6 = [9,10,10,9,9] 
        lx6 = [5,5,6,6,5]
        plt.plot(lx6,ly6,color=door_color,linewidth=thick,zorder=5)
        #id=3
        ly7 = [15,16,16,15,15]
        lx7 = [12,12,13,13,12]
        plt.plot(lx7,ly7,color=door_color,linewidth=thick,zorder=5)
        #id=4
        ly8 = [13,14,14,13,13] 
        lx8 = [16,16,17,17,16]
        plt.plot(lx8,ly8,color=door_color,linewidth=thick,zorder=5)
        #rysowanie utrudnień
        x = [3.5,3.5,3.5,3.5,3.5]
        y = [3.5,4.5,5.5,6.5,7.5]
        points_whole_ax = 2 * 0.8 * 72    # 1 point = dpi / 72 pixels
        radius = 0.1
        points_radius = 2 * radius / 1.0 * points_whole_ax
        self.ax.scatter(x, y, s=points_radius**2)
        #rysowanie siatki
        tick_spacing = 1
        self.ax.xaxis.set_major_locator(ticker.MultipleLocator(tick_spacing))
        self.ax.yaxis.set_major_locator(ticker.MultipleLocator(tick_spacing))

    def create_canvas_with_animation(self):
        self.create_building()
        self.canvas = FigureCanvasTkAgg(self.fig,self.master) 
        self.canvas.draw() 
        self.canvas.get_tk_widget().grid(row=1,column=1) 
        self.toolbar_frame = Frame(self.master)
        self.toolbar_frame.grid(row=2,column=3)
        self.toolbar = NavigationToolbar2Tk(self.canvas,self.toolbar_frame)
        self.toolbar.config(bg=COLOR_GREEN)
        self.toolbar.update()           

    def create_plot_button(self):
        self.frame2=Frame(self.master,bg=COLOR_GREEN)
        self.frame2.grid(row=1, column=2)
        #Przycisk startu symulacji
        self.plot_start_button=Button(self.frame2, text="START SIMULATION", 
                                bg=COLOR_SELEDINE,font=FONT,
                                activebackground=COLOR_GREEN,
                                command =self.start_simulation, width=29)
        self.plot_start_button.pack()
        self.create_pause_button()
        self.create_unpause_button()
        self.label_1=Label(self.frame2,text="",
                             bg=COLOR_GREEN,borderwidth=2, relief="groove",width=33,height=2)
        self.label_1.pack()
        self.label_2=Label(self.frame2,text="",
                             bg=COLOR_GREEN,borderwidth=2, relief="groove",width=33,height=2)
        self.label_2.pack()
        self.label_3=Label(self.frame2,text="",
                             bg=COLOR_GREEN,borderwidth=2, relief="groove",width=33,height=2)
        self.label_3.pack()
        self.label_4=Label(self.frame2,text="",
                             bg=COLOR_GREEN,borderwidth=2, relief="groove",width=33,height=2)
        self.label_4.pack()
        
    def load_simulation_data(self):
        self.my_data = genfromtxt("src/main/resources/visualisation.csv", delimiter='.',dtype=str)
#        print(self.my_data[1,1])
#        print(self.my_data.shape[0])
        xs=[]
        ys=[]
        ss=[]  
        arr=[]
        d=""
        a = np.zeros((self.my_data.shape[0], self.my_data.shape[1]))
        k=(self.my_data.shape[1])-2
        a=np.delete(self.my_data,0,1)
        d=np.delete(a,0,0)
        c=np.delete(a,k,1)
        b=np.delete(c,0,0)
        f=d[:,k]
        #czyszczenie danych z pliku csv
        for i in range((self.my_data.shape[0])-1):
            v=f[i]
            v=v.replace('[', '')
            v=v.replace(']', '')
            v=v.replace('(', '')
            v=v.replace(')', '')
            v = [x.strip() for x in v.split(',')]
            arr.append(len(v)/2)
        self.np_arr = np.array(arr) 
        #dopasowanie danych do wykreslania symulacji - dane ognia
        ne= []
        self.ney=[]
        self.nex=[]          
        vx=v[::2]
        vy=v[1::2]
        for item in v:
            ne.append(float(item))
        for item in vy:
            self.ney.append(float(item))
        for item in vx:
            self.nex.append(float(item))
        self.ney=np.add(self.ney,0.5)
        self.nex=np.add(self.nex,0.5)
        #dopasowanie danych do wykreslania symulacji - dane ludzi
        for x in b:
            for z in x:
                d=z
                d=d.replace('(', '')
                d=d.replace(')', '')
                result = [x.strip() for x in d.split(',')] 
                xs.append(result[0])
                ys.append(result[1])
                ss.append(result[2])
        np_x=np.asarray(xs)
        np_y=np.asarray(ys)
        np_s=np.asarray(ss)
        self.xval= np_x.reshape(b.shape[0], b.shape[1])
        self.xval=y = self.xval.astype(np.float)
        self.yval= np_y.reshape(b.shape[0], b.shape[1])
        self.yval = self.yval.astype(np.float)
        self.sval= np_s.reshape(b.shape[0], b.shape[1])
        self.turn_data_into_plot_color()
        
    def turn_data_into_plot_color(self):
        self.nr_normal=[]
        self.nr_safe=[]
        self.nr_cantmove=[]
        g = 0
        h=0
        j=0
        k=0
        for x in self.sval:
            g=0
            h=0
            j=0
            k=0
            for z in x:
                if(z=='NORMAL'):
                    g+=1
                elif(z=='SAFE'):
                    h+=1
                elif(z=='CANT_MOVE'):
                    j+=1
                elif(k=='PANIC'):
                    k+=1
            self.nr_normal.append(g)
            self.nr_safe.append(h)
            self.nr_cantmove.append(j)
        for x in range(0, self.sval.shape[0]):
            for y in range(0, self.sval.shape[1]):
                if(self.sval[x,y]=='NORMAL'):
                    g+=1
                    self.sval[x,y]='blue'
                elif(self.sval[x,y]=='SAFE'):
                    h+=1
                    self.sval[x,y]='purple'
                elif(self.sval[x,y]=='CANT_MOVE'):
                    j+=1
                    self.sval[x,y]='red'
                elif(self.sval[x,y]=='PANIC'):
                    k+=1
                    self.sval[x,y]='yellow'
        self.xval=np.add(self.xval,0.5)
        self.yval=np.add(self.yval,0.5)
        
    def update_plot(self,i, fig, scat,x,y):
        data = np.c_[(self.xval[i,:], self.yval[i,:])]
        scat.set_offsets(data)
        scat.set_color(self.sval[i,:])
        self.label_1['text']=('Runda: %d' %(i+1))
        return scat,  

    def update_plot_2(self,i, fig, scat,x,y):
        k=int(self.np_arr[i])
        data = np.c_[(self.nex[:k], self.ney[:k])]
        t1=self.nr_normal[i]
        t2=self.nr_safe[i]
        t3=self.nr_cantmove[i]
        self.label_2['text']=('NR NORMAL STATUS people: '+str(t1))
        self.label_3['text']=('NR SAFE STATUS people: '+str(t2))
        self.label_4['text']=('NR CANT_MOVE STATUS people: '+str(t3))
        scat.set_offsets(data)
        return scat,    
    
    def start_simulation(self):
        self.plot_start_button['state'] = 'disabled'
        self.nr_of_rounds=int(self.my_data.shape[0]-1)
        a = self.nex[0]
        b = self.ney[0]
        scat=self.ax.scatter(self.xval[0,:],self.yval[0,:],s=300,label="People")
        scat2=self.ax.scatter(a,b,color="red",marker="^",s=300,label="Fire")
        self.anim = animation.FuncAnimation(self.fig, self.update_plot, fargs = (self.fig, scat,self.xval[0,:],self.yval[0,:]),
                               frames = self.nr_of_rounds, interval =1000,repeat=True)
        self.canvas.draw()
        self.anim2 = animation.FuncAnimation(self.fig, self.update_plot_2, fargs = (self.fig, scat2,a,b),
                               frames = self.nr_of_rounds, interval = 1000,repeat=True)
        self.canvas.draw()
        self.ax.legend(loc='upper left')
        plt.title("Fire Spread & People Escape Simulation")
        
    def create_pause_button(self):
        self.pause_btn=Button(self.frame2, text="PAUSE", 
                bg=COLOR_SELEDINE,font=FONT,
                activebackground=COLOR_GREEN,command =self.pause_plot_onClick, width=29)
        self.pause_btn.pack()
        
    def pause_plot_onClick(self):
        self.anim.event_source.stop()
        self.anim2.event_source.stop()
    
    def create_unpause_button(self):
        self.unpause_btn=Button(self.frame2, text="UNPAUSE", 
            bg=COLOR_SELEDINE,font=FONT,
            activebackground=COLOR_GREEN,command =self.unpause_plot_onClick, width=29)
        self.unpause_btn.pack()
        
    def unpause_plot_onClick(self):
        self.anim.event_source.start()
        self.anim2.event_source.start()
        
    def init_gui(self):
        self.create_canvas_with_animation()
        self.create_plot_button()
        self.load_simulation_data()
        
            
if __name__ == '__main__':
    root = Tk()
    my_gui = View(root)
    root.mainloop()
