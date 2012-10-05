#
# RHQ WebSphere Plug-in
# Copyright (C) 2012 Crossroads Bank for Social Security
# All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License, version 2, as
# published by the Free Software Foundation, and/or the GNU Lesser
# General Public License, version 2.1, also as published by the Free
# Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License and the GNU Lesser General Public License
# for more details.
#
# You should have received a copy of the GNU General Public License
# and the GNU Lesser General Public License along with this program;
# if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#

import AdminConfig

def wsadminToList(inStr):
	outList=[]
	if (len(inStr)>0 and inStr[0]=='[' and inStr[-1]==']'):
		inStr = inStr[1:-1]
		tmpList = inStr.split(" ")
	else:
		tmpList = inStr.split("\n") #splits for Windows or Linux
	for item in tmpList:
		item = item.rstrip();       #removes any Windows "\r"
		if (len(item)>0):
			outList.append(item)
	return outList

def getConfigObjects(filter):
	objects = []
	for id in wsadminToList(AdminConfig.getid(filter)):
		objects.append(ConfigObject(id))
	return objects

def getUniqueConfigObject(filter):
    objects = getConfigObjects(filter)
    if len(objects) == 0:
        raise Error("No config object found for " + filter)
    if len(objects) > 1:
        raise Error("More than one config object found for " + filter)
    return objects[0]

def getServers(types):
	servers = []
	for server in wsadminToList(AdminConfig.getid("/Server:/")):
		if AdminConfig.showAttribute(server, "serverType") in types:
			servers.append(ConfigObject(server))
	return servers
    
# Get all servers including node agents and deployment managers, but excluding HTTP servers
def getAllServers():
    return getServers([ "APPLICATION_SERVER", "NODE_AGENT", "DEPLOYMENT_MANAGER" ])

def getApplicationServers():
    return getServers([ "APPLICATION_SERVER" ])

class ConfigObject:
	def __init__(self, id):
		self.id = id
	def getAttribute(self, attrName):
		return AdminConfig.showAttribute(self.id, attrName)
	def setAttribute(self, attrName, attrValue):
		AdminConfig.modify(self.id, [[attrName, attrValue]])
	def getChild(self, attrName):
		childId = AdminConfig.showAttribute(self.id, attrName)
		if childId:
			return ConfigObject(childId)
	def getChildren(self, attrName):
		children = []
		for childId in wsadminToList(AdminConfig.showAttribute(self.id, attrName)):
			children.append(ConfigObject(childId))
		return children

class PropertySet:
    def __init__(self, obj):
        self.obj = obj
    def get(self, name):
        for prop in self.obj.getChildren('resourceProperties'):
            if prop.getAttribute('name') == name:
                return prop.getAttribute('value')
    def set(self, name, value, desc, type):
        for prop in self.obj.getChildren('resourceProperties'):
            if prop.getAttribute('name') == name:
                prop.setAttribute('value', value)
                return
        AdminConfig.create('J2EEResourceProperty', self.obj.id, [['name', name], ['value', value], ['description', desc], ['type', type]])
