# -*- coding: <utf-8> -*-
from django.contrib import admin
from models import *



# FK lookup support override. TODO: in Django 1.3 this should be supported out-of-the-box
from django.db.models.fields import FieldDoesNotExist
import copy
from django.db.models.fields.related import ForeignKey
from django.db.models import options

def get_field(self, name, many_to_many=True):
	"""
	Returns the requested field by name. Raises FieldDoesNotExist on error.
	"""
	to_search = many_to_many and (self.fields + self.many_to_many) or self.fields
	if hasattr(self, '_copy_fields'):
		to_search += self._copy_fields
	for f in to_search:
		if f.name == name:
			return f
	if not name.startswith('__') and '__' in name:
		f = None
		model = self
		path = name.split('__')
		for field_name in path:
			f = model._get_field(field_name)
			if isinstance(f, ForeignKey):
				model = f.rel.to._meta
		f = copy.deepcopy(f)
		f.name = name
		if not hasattr(self, "_copy_fields"):
			self._copy_fields = list()
		self._copy_fields.append(f)
		return f
	raise FieldDoesNotExist, '%s has no field named %r' % (self.object_name, name)

setattr(options.Options, '_get_field', options.Options.get_field.im_func)
setattr(options.Options, 'get_field', get_field)
#######



class IconMeaningInline(admin.StackedInline):
    model = IconMeaning
    extra = 0

class IconAdmin(admin.ModelAdmin):
    inlines = [IconMeaningInline, ]
    list_display = [ 'icon_html', 'icon'] 
    
class IconCategoryAdmin(admin.ModelAdmin):
    list_display = [ 'icon_html', 'title', 'export_to_mobile', 'processed',  'title_fr_mobile', 'title_fr_long', 'title_en_mobile', '__unicode__']     
    list_editable = [ 'title_en_mobile', 'title_fr_long', 'title_fr_mobile', 'export_to_mobile', 'processed', ]
    
class IconMeaningAdmin(admin.ModelAdmin):   
    list_display = [ 'icon_html', 'text', 'color_code', 'part_of_speech', 'category_title', 'lang'] 
    list_filter = ['color_code', 'part_of_speech',   'category__title', 'lang']
    search_fields = ['text']



admin.site.register(Icon, IconAdmin)
admin.site.register(IconMeaning, IconMeaningAdmin)
admin.site.register(IconCategory, IconCategoryAdmin)
