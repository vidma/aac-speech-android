#!/usr/bin/python
# -*- coding: <encoding name> -*-

from django.db import models
from settings import _
from django.core.exceptions import ObjectDoesNotExist    

from easy_thumbnails.files import get_thumbnailer

# Create your models here.

PART_OF_SPEECH_CHOICES = [
('NOUN', _('Noun')),
('VERB', _('Verb')),
('ADJECTIVE', _('Adjective')),
('ADVERB', _('Adverb')),
]

class Icon(models.Model):

    icon = models.FileField(upload_to='icons', blank=False)
    
    color_mode = models.IntegerField(blank = True, null = True, choices = [(0, "B&W"), (1, "Color")])

	


    def icon_thumbnail(self):
        source = self.icon
        thumbnail_options = dict(size=(64, 64), crop=False, bw=False)
        
        return get_thumbnailer(source).get_thumbnail(thumbnail_options).url

    def icon_html(self):
        url = self.icon_thumbnail()
        return '<img src="%s" style="width: 64px" />' % url
        
    icon_html.allow_tags = True

class IconCategory(models.Model):
    title = models.CharField(max_length=250, default='', blank=True) # e.g. place
    machine_title = models.CharField(max_length=250, default='', blank=True) # e.g. place   
     


        
    default_part_of_speech = models.CharField(max_length=250, default='', blank=True, choices = PART_OF_SPEECH_CHOICES) # e.g. TODO: in fact there may be multiple meanings/values    
    
    title_en_mobile   = models.CharField(max_length=250, default='', blank=True) # e.g. short title
    title_fr_mobile   = models.CharField(max_length=250, default='', blank=True) # e.g. short title for mobiles
    
    title_fr_long   = models.CharField(max_length=250, default='', blank=True) # e.g. short title for mobiles
    
    order = models.IntegerField(blank = True, null = False, default=0)
    
    #  state in processing workflow
    export_to_mobile = models.BooleanField(blank = True, null = False, default=False)
    processed = models.BooleanField(blank = True, null = False, default=False)
    
    icon = models.ForeignKey(Icon, related_name='in_category_set')     
    
    def __unicode__(self):
        # TODO: Hmm, why not as many on the device: maybe trim is needed or whatewa?
        count = len(self.icons.values('text').distinct().all())
        return "%s ( # %d unique):  id=%d" % (self.title, count, self.id) #  + " (%d)"% count
    __repr__ = __unicode__    
    
    
    def icon_thumbnail(self):
        print "thumb"
        print self.icon
        if not self.icon:
            return ""
            
        source = self.icon.icon

            
        thumbnail_options = dict(size=(64, 64), crop=False, bw=False)
        
        return get_thumbnailer(source).get_thumbnail(thumbnail_options).url

    def icon_html(self):
        url = self.icon_thumbnail()
        return '<img src="%s" style="width: 64px" />' % url
    icon_html.allow_tags = True
    

        
class IconMeaning(models.Model):
    lang = models.CharField(max_length=250, default='', blank=True, choices = [('fr', 'French'), ('en', 'English')]) # e.g. EN, ...
    text = models.CharField(max_length=250, default='', blank=True) # e.g. TODO: in fact there may be multiple meanings/values
    part_of_speech = models.CharField(max_length=250, default='', blank=True, choices = PART_OF_SPEECH_CHOICES) # e.g. TODO: in fact there may be multiple meanings/values
    
    #TODO: part of speech approved
    #TODO:  categorization approved

    def get_color_count(color):
        return " (%d)" % len(IconMeaning.objects.filter(color_code=color))
        
    # a SPC color list	     
    spc_colors = [(1, "Proper names" , "yellow"),
        (2, "Common names" , "orange"),
        (3, "Actions" , "green"),
        (4, "Descriptives (adjectives, adverbs,...)" , "blue"),
        (5, "Social" ,  "pink"),
        (6, "Miscellanea" , "white"),]
    color_choices = map(lambda (color_id, word_type, color): (color_id, "%s: %s" % (word_type, color)), spc_colors)
    color_code = models.IntegerField(blank = True, null = True, choices = color_choices)

    # 801 vs 795 - 6 repeatIconMeaning.objects.
    category = models.ForeignKey(IconCategory, related_name='icons', null=True)

    icon = models.ForeignKey(Icon, related_name='meaning_set')     
    
    # states of items processing workflow
    part_of_speech_tag_processed = models.BooleanField(blank = True, null = False, default=False)
    prefered_processed = models.BooleanField(blank = True, null = False, default=False)
    
    # in case of multiple icons for the same word, shall this one be chosen
    prefered_icon = models.BooleanField(blank = True, null = False, default=False)
    
    def category_title(self):
        return self.category.title

    def icon_thumbnail(self):
        source = self.icon.icon
        thumbnail_options = dict(size=(64, 64), crop=False, bw=False)
        
        return get_thumbnailer(source).get_thumbnail(thumbnail_options).url

    def icon_html(self):
        url = self.icon.icon.url
        url = self.icon_thumbnail()
        return '<img src="%s" style="width: 64px" />' % url
        
    icon_html.allow_tags = True

    def __unicode__(self):
        return self.text
    __repr__ = __unicode__
                         
    # also there may be more than one icon with the same meaning!

    # TODO: Icon may or may not have a position (row, col) assigned in a "category/word_table"; also these may be generated automatically from category/color_code


    
    
def sql_escape(string):
    #TODO:
    return string # string.replace("'", "\\'")

def to_ascii_printable(string):
  import unicodedata
  return unicodedata.normalize('NFKD', unicode(string)).encode('ASCII', 'ignore')



def sqlite_gen_icons_for_lang(lang = 'fr'):
    # TODO: multilanguage
    """ generates sqlite insert statements like the following:
    INSERT INTO icon_meanings (word, word_type, color_code, icon_path, lang) VALUES ('supprimer', 'verb', 'file:///sdcard/acc_icons/11196_1.png.64x64_q85.png', 'fr');;"""


    """
    1) we may have douplicates and some of them may be better tagged than others
        - combine their categories
    2) we may have multiple categories per word
    """
    
    queries = []
    values = []
    doublicate_items = {}
    multi_icon_items = {}    
    i = 0
    for meaning in IconMeaning.objects.all().filter(lang=lang):            
        word = sql_escape(meaning.text)
        word_ascii = to_ascii_printable(word)
        color_code = meaning.color_code
        icon_path = 'file:///sdcard/acc_icons/'+meaning.icon_thumbnail().replace('/media/icons/', '')
        
        # TODO: categories... hmmm there are only 6 items that belong to more than one category
        # I will define this as the main category
        # use an id, as DB join, grouping and selection are much quicker by int than string
        #TODO: I shall use initial (permanent IDs)

        
        # TODO: part of speech tagging!!!
        # TODO: For now let's use the colorcode                
        part_of_speech = 'noun'
        if color_code == 3:
            part_of_speech = 'verb'
        if color_code == 4:      
            # we may have adverbs too. do we have any?
            part_of_speech = 'adjective'      
        
        # Doublicates check
        # currently we have both doublicate icon/meaning pairs (multiple records for exactly the same), and also multiple icons for the same word
        # TODO: later we shall manually process the entries to select the best icon, current I select the first one in case of douplicates!
        # TODO: check if such a scenario is possible: to words are spelled the same, but their icon and part_of_speech are different
        doub_key = word
        
        #make_hash = lambda f: f # TODO: this shall return a hash based on file contents
        #multi_icon_key = meaning + make_hash(icon_path)
        if not doublicate_items.has_key(doub_key):
             doublicate_items[doub_key] = 0
        doublicate_items[doub_key] = doublicate_items[doub_key] + 1

        # for now Take the first icon
        if doublicate_items[doub_key] > 1:
            continue
        
        
        i = i + 1
        if i % 500 == 0:
            print i
            
        categories = []
        
        # returns a list of dicts: [{'category__id': 12L}]
        all_duplicates = IconMeaning.objects.filter(text=meaning.text).exclude(category__id = None).values('category__id').distinct()
        for duplicate in all_duplicates:
            categories.append(duplicate['category__id'])
        if (len(categories) > 1):
            print "More than one category (%d) for: %s" % (len(categories), word_ascii)
        category_list = ",".join([str(c) for c in categories])            
            
        main_category_id = (categories and categories[0]) or 0
        # 38 in total! though I may have had a douplicate that has no category at all before!
            


        # TODO: Remember What is the main category being use for???
        queries.append("%(word)s|%(word_ascii)s|%(part_of_speech)s|%(color_code)d|%(icon_path)s|%(lang)s|%(main_category_id)d|%(category_list)s" %  locals())

    print "N of Douplicates: %d" % sum([v-1 for v in doublicate_items.values() if v > 1])     
    #print unicode.encode(" ".join(doublicate_items.keys()), "utf-8")        
    return queries
        # TODO: part of speech = int would be more performant
        
        
def sqlite_gen_icons():
    queries_en = sqlite_gen_icons_for_lang(lang = 'en')
    queries_fr = sqlite_gen_icons_for_lang(lang = 'fr')
    
    with open('icon_meanings.data', 'w') as f:
         f.write(unicode.encode('\n'.join(queries_fr), "utf-8"))
         f.write(unicode.encode('\n'.join(queries_en), "utf-8"))
         

def sqlite_gen_categories():
    queries = []
    for cat in IconCategory.objects.filter(export_to_mobile=True):
        cat_id = cat.id
        try:
          icon_path = 'file:///sdcard/category_icons/'+cat.icon_thumbnail().replace('/media/icons/', '')
        except ObjectDoesNotExist:
          icon_path =""
          print "no icon for id=%d" % cat_id
        #TODO: copy category icon to right export folder
        
        title_short, title_long = cat.title_fr_mobile, cat.title_fr_long
        lang = 'fr'
        queries.append("%(cat_id)d|%(title_short)s|%(title_long)s|%(icon_path)s|%(lang)s" %  locals())
        
        title_short, title_long = cat.title_en_mobile, cat.title
        lang = 'en'        
        queries.append("%(cat_id)d|%(title_short)s|%(title_long)s|%(icon_path)s|%(lang)s" %  locals())
        
    with open('categories.data', 'w') as f:
         f.write(unicode.encode('\n'.join(queries), "utf-8"))    
            
"""
Usage (on ./manage.py shell):
from acc.icons.models import *
sqlite_gen_categories()
sqlite_gen_icons()
"""

        




from acc.settings import MEDIA_ROOT
from django.core.files import File
import os


def import_add_icon_meanings(icon_instance, phrases, lang='fr'):
    for phrase_item in phrases:
        try:
            (phrase, psc_color) = phrase_item
            icon_meaning = IconMeaning(text=phrase, color_code = psc_color, lang = lang)
            icon_meaning.icon = icon_instance
            icon_meaning.save()
        except Exception, e:
            print icon, phrases
            print e


def do_import():  
    with open('color.csv', 'r') as f:
        lines = f.readlines()  
        table =[ c.replace("\n", "").split("\t") for c in lines if c ]
        for icon in table:
            try:
                (icon_id, spanish, english, french) = icon
                phrases = [phrase.split("=") for phrase in french.split(";") if phrase]
                phrases_en = [phrase.split("=") for phrase in english.split(";") if phrase]
                
                if not phrases and not phrases_en:
                    #print "nothing to do for this icon"
                    #print icon, phrases
                    continue
                #print (icon_id, spanish, english, french)
                
                # check if icon exists, and create one if not
                try:
                     icon_instance = Icon.objects.get(pk=icon_id)
                except ObjectDoesNotExist:    
                    print "Creating icon id=%s" % icon_id
                    
                    icon_instance = Icon(color_mode = 1, id=icon_id)
                    
                    icon_path = os.path.join(MEDIA_ROOT, "Pictogramas_Color_ID/%s.png" % icon_id)
                    with open(icon_path, 'r') as icon_f:
                        ff = File(icon_f)
                        icon_instance.icon.save('%s.png' % icon_id, ff, save=False)
                        icon_instance.save()
                        
                #TODO: handle douplicates and updates:  import_add_icon_meanings(icon_instance, phrases, lang='fr')
                if phrases_en:
                    import_add_icon_meanings(icon_instance, phrases_en, lang='en')
                    
                """ for phrase_item in phrases:
                    try:
                        (phrase, psc_color) = phrase_item
                        icon_meaning = IconMeaning(text=phrase, color_code = psc_color, lang = 'fr')
                        icon_meaning.icon = icon_instance
                        icon_meaning.save()
                    except Exception, e:
                        print icon, phrases
                        print e """
                        
                        
            except Exception, e:
                print e
                print icon
            
    # TODO: generate a 64x64 thumbnail for android phones!!!
    #TODO: import categories
    

def cat_import():  
    with open('categories.csv', 'r') as f:
        lines = f.readlines()
        catdef = [c.replace("\n", "").split("\t") for c in lines if c.replace("\n", "")]
        
        for cat in catdef:
            (img_id, cat_name) = cat
            print (img_id, cat_name)
            
            try:
                cat = IconCategory.objects.get(machine_title=cat_name)
            except ObjectDoesNotExist:
                print "do not exist: creating cat:" + cat_name
                cat = IconCategory(machine_title=cat_name, title=cat_name.replace("_", " "))
                cat.save()
            try:
                icon = Icon.objects.get(id=img_id)
                
                for meaning in icon.meaning_set.all():
                    meaning.category = cat
                    meaning.save()
                    
            except ObjectDoesNotExist:
                print "icon with id=%s do not exist!!!" % img_id

"""
Usage:
from acc.icons.models import *
do_import();
cat_import();
"""

