<?php  // $Id$

/**
 * This file contains the parent class for moodle blocks, block_base.
 *
 * @author Jon Papaioannou
 * @version  $Id$
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package blocks
 */

/// Constants

/**
 * Block type of list. Contents of block should be set as an associative array in the content object as items ($this->content->items). Optionally include footer text in $this->content->footer.
 */
define('BLOCK_TYPE_LIST',    1);

/**
 * Block type of text. Contents of block should be set to standard html text in the content object as items ($this->content->text). Optionally include footer text in $this->content->footer.
 */
define('BLOCK_TYPE_TEXT',    2);

/**
 * Class for describing a moodle block, all Moodle blocks derive from this class
 *
 * @author Jon Papaioannou
 * @package blocks
 */
class block_base {

    /**
     * Internal var for storing/caching translated strings
     * @var string $str
     */
    var $str;

    /**
     * The title of the block to be displayed in the block title area.
     * @var string $title
     */
    var $title         = NULL;

    /**
     * The type of content that this block creates. Currently support options - BLOCK_TYPE_LIST, BLOCK_TYPE_TEXT
     * @var int $content_type
     */
    var $content_type  = BLOCK_TYPE_TEXT;

    /**
     * An object to contain the information to be displayed in the block.
     * @var stdObject $content
     */
    var $content       = NULL;

    /**
     * A string generated by {@link _add_edit_controls()} to display block manipulation links when the user is in editing mode.
     * @var string $edit_controls
     */
    var $edit_controls = NULL;

    /**
     * The current version that the block type defines.
     * @var string $version
     */
    var $version       = NULL;

    /**
     * The initialized instance of this block object.
     * @var block $instance
     */
    var $instance      = NULL;

    /**
     * An object containing the instance configuration information for the current instance of this block.
     * @var stdObject $config
     */
    var $config        = NULL;

    /** 
     * How often the cronjob should run, 0 if not at all.
     * @var int $cron
     */

    var $cron          = NULL;

    /**
     * Indicates blocked is pinned - can not be moved, always present, does not have own context
     */
    var $pinned        = false;

/// Class Functions

    /**
     * The class constructor
     *
     */
    function block_base() {
        $this->init();
    }

    /**
     * Fake constructor to keep PHP5 happy
     *
     */
    function __construct() {
        $this->block_base();
    }
    
    /** 
     * Function that can be overridden to do extra setup after
     * the database install. (Called once per block, not per instance!)
     */
    function after_install() {
    }
    
    /**
     * Function that can be overridden to do extra cleanup before
     * the database tables are deleted. (Called once per block, not per instance!)
     */
    function before_delete() {
    }
    
    /**
     * Function that can be overridden to do extra setup after a block instance has been
     * restored from backup. For example, it may need to alter any dates that the block
     * stores, if the $restore->course_startdateoffset is set.  
     */
    function after_restore($restore) {
    }

    /**
     * Enable custom instance data section in backup and restore.
     * 
     * If return true, then {@link instance_backup()} and
     * {@link instance_restore()} will be called during
     * backup/restore routines.
     *
     * @return boolean
     **/
    function backuprestore_instancedata_used() {
        return false;
    }

    /**
     * Allows the block class to have a backup routine.  Handy 
     * when the block has its own tables that have foreign keys to 
     * other tables (example: user table).
     * 
     * Note: at the time of writing this comment, the indent level 
     * for the {@link full_tag()} should start at 5.
     *
     * @param resource $bf Backup File
     * @param object $preferences Backup preferences
     * @return boolean
     **/
    function instance_backup($bf, $preferences) {
        return true;
    }

    /**
     * Allows the block class to restore its backup routine.
     * 
     * Should not return false if data is empty 
     * because old backups would not contain block instance backup data.
     * 
     * @param object $restore Standard restore object
     * @param object $data Object from backup_getid for this block instance
     * @return boolean
     **/
    function instance_restore($restore, $data) {
        return true;
    }

    /**
     * Will be called before an instance of this block is backed up, so that any links in
     * in config can be encoded. For example config->text, for the HTML block
     * @return string
     */
    function get_backup_encoded_config() {
        return base64_encode(serialize($this->config));
    }

    /**
     * Return the content encoded to support interactivities linking. This function is
     * called automatically from the backup procedure by {@link backup_encode_absolute_links()}.
     *
     * NOTE: There is no block instance when this method is called.
     *
     * @param string $content Content to be encoded
     * @param object $restore Restore preferences object
     * @return string The encoded content
     **/
    function encode_content_links($content, $restore) {
        return $content;
    }

    /**
     * This function makes all the necessary calls to {@link restore_decode_content_links_worker()}
     * function in order to decode contents of this block from the backup 
     * format to destination site/course in order to mantain inter-activities 
     * working in the backup/restore process. 
     * 
     * This is called from {@link restore_decode_content_links()} function in the restore process.
     *
     * NOTE: There is no block instance when this method is called.
     *
     * @param object $restore Standard restore object
     * @return boolean
     **/
    function decode_content_links_caller($restore) {
        return true;
    }

    /**
     * Return content decoded to support interactivities linking.
     * This is called automatically from
     * {@link restore_decode_content_links_worker()} function
     * in the restore process.
     *
     * NOTE: There is no block instance when this method is called.
     *
     * @param string $content Content to be dencoded
     * @param object $restore Restore preferences object
     * @return string The dencoded content
     **/
    function decode_content_links($content, $restore) {
        return $content;
    }

    /**
     * Returns the block name, as present in the class name,
     * the database, the block directory, etc etc.
     *
     * @return string
     */
    function name() {
        // Returns the block name, as present in the class name,
        // the database, the block directory, etc etc.
        static $myname;
        if ($myname === NULL) {
            $myname = strtolower(get_class($this));
            $myname = substr($myname, strpos($myname, '_') + 1);
        }
        return $myname;
    }

    /**
     * Parent class version of this function simply returns NULL
     * This should be implemented by the derived class to return
     * the content object.
     *
     * @return stdObject
     */
    function get_content() {
        // This should be implemented by the derived class.
        return NULL;
    }

    /**
     * Returns the class $title var value.
     *
     * Intentionally doesn't check if a title is set. 
     * This is already done in {@link _self_test()}
     *
     * @return string $this->title
     */
    function get_title() {
        // Intentionally doesn't check if a title is set. This is already done in _self_test()
        return $this->title;
    }

    /**
     * Returns the class $content_type var value.
     *
     * Intentionally doesn't check if content_type is set. 
     * This is already done in {@link _self_test()}
     *
     * @return string $this->content_type
     */
    function get_content_type() {
        // Intentionally doesn't check if a content_type is set. This is already done in _self_test()
        return $this->content_type;
    }

    /**
     * Returns the class $version var value.
     *
     * Intentionally doesn't check if a version is set. 
     * This is already done in {@link _self_test()}
     *
     * @return string $this->version
     */
    function get_version() {
        // Intentionally doesn't check if a version is set. This is already done in _self_test()
        return $this->version;
    }

    /**
     * Returns true or false, depending on whether this block has any content to display
     * and whether the user has permission to view the block
     *
     * @return boolean
     */
    function is_empty() {

        if (empty($this->instance->pinned)) {
            $context = get_context_instance(CONTEXT_BLOCK, $this->instance->id);
        } else {
            $context = get_context_instance(CONTEXT_SYSTEM); // pinned blocks do not have own context
        }
        
        if ( !has_capability('moodle/block:view', $context) ) {
            return true;
        }

        $this->get_content();
        return(empty($this->content->text) && empty($this->content->footer));
    }

    /**
     * First sets the current value of $this->content to NULL
     * then calls the block's {@link get_content()} function
     * to set its value back.
     *
     * @return stdObject
     */
    function refresh_content() {
        // Nothing special here, depends on content()
        $this->content = NULL;
        return $this->get_content();
    }

    /**
     * Display the block!
     */
    function _print_block() {
        global $COURSE;

        // is_empty() includes a call to get_content()
        if ($this->is_empty() && empty($COURSE->javascriptportal)) {
            if (empty($this->edit_controls)) {
                // No content, no edit controls, so just shut up
                return;
            } else {
                // No content but editing, so show something at least
                $this->_print_shadow();
            }
        } else {
            if ($this->hide_header() && empty($this->edit_controls)) {
                // Header wants to hide, no edit controls to show, so no header it is
                print_side_block(NULL, $this->content->text, NULL, NULL, $this->content->footer, $this->html_attributes());
            } else {
                // The full treatment, please. Include the title text.
                print_side_block($this->_title_html(), $this->content->text, NULL, NULL, $this->content->footer, $this->html_attributes(), $this->title);
            }
        }
    }

    /**
     * Block contents are missing. Simply display an empty block so that
     * edit controls are accessbile to the user and they are aware that this
     * block is in place, even if empty.
     */
    function _print_shadow() {
        print_side_block($this->_title_html(), '&nbsp;', NULL, NULL, '', array('class' => 'hidden'), $this->title);
    }


    function _title_html() {
        global $CFG;

        //Accessibility: validation, can't have <div> inside <h2>, use <span>.
        $title = '<div class="title">';

        if (!empty($CFG->allowuserblockhiding)) {
            //Accessibility: added 'alt' text for the +- icon.
            //Theme the buttons using, Admin - Miscellaneous - smartpix.
            $strshow = addslashes_js(get_string('showblocka', 'access', strip_tags($this->title)));
            $strhide = addslashes_js(get_string('hideblocka', 'access', strip_tags($this->title)));
            $title .= '<input type="image" src="'.$CFG->pixpath.'/t/switch_minus.gif" '. 
                'id="togglehide_inst'.$this->instance->id.'" '.
                'onclick="elementToggleHide(this, true, function(el) {'.
                'return findParentNode(el, \'DIV\', \'sideblock\'); },'.
                ' \''.$strshow.'\', \''.$strhide.'\'); return false;" '.
                'alt="'.$strhide.'" title="'.$strhide.'" class="hide-show-image" />';
        }

        //Accesssibility: added H2 (was in, weblib.php: print_side_block)
        $title .= '<h2>'.$this->title.'</h2>';

        if ($this->edit_controls !== NULL) {
            $title .= $this->edit_controls;
        }

        $title .= '</div>';
        return $title;
    }

    /**
     * Sets class $edit_controls var with correct block manipulation links.
     *
     * @uses $CFG
     * @uses $USER
     * @param stdObject $options ?
     * @todo complete documenting this function. Define $options.
     */
    function _add_edit_controls($options) {
        global $CFG, $USER, $PAGE;
        
        if (empty($this->instance->pinned)) {
            $context = get_context_instance(CONTEXT_BLOCK, $this->instance->id);
        } else {
            $context = get_context_instance(CONTEXT_SYSTEM); // pinned blocks do not have own context
        }
        
        // context for site or course, i.e. participant list etc
        // check to see if user can edit site or course blocks.
        // blocks can appear on other pages such as mod and blog pages...

        switch ($this->instance->pagetype) {
            case 'course-view':
                if (!has_capability('moodle/site:manageblocks', $context)) {
                    return null;
                }
            break;
            default:
            
            break;  
        }
        
        
        if (!isset($this->str)) {
            $this->str->delete    = get_string('delete');
            $this->str->moveup    = get_string('moveup');
            $this->str->movedown  = get_string('movedown');
            $this->str->moveright = get_string('moveright');
            $this->str->moveleft  = get_string('moveleft');
            $this->str->hide      = get_string('hide');
            $this->str->show      = get_string('show');
            $this->str->configure = get_string('configuration');
            $this->str->assignroles = get_string('assignroles', 'role');
        }

        // RTL support - exchange right and left arrows
        if (right_to_left()) {
            $rightarrow = 'left.gif';
            $leftarrow  = 'right.gif';
        } else {
            $rightarrow = 'right.gif';
            $leftarrow  = 'left.gif';
        }

        $movebuttons = '<div class="commands">';

        if ($this->instance->visible) {
            $icon = '/t/hide.gif';
            $title = $this->str->hide;
        } else {
            $icon = '/t/show.gif';
            $title = $this->str->show;
        }

        if (empty($this->instance->pageid)) {
            $this->instance->pageid = 0;
        }
        if (!empty($PAGE->type) and ($this->instance->pagetype == $PAGE->type) and $this->instance->pageid == $PAGE->id) {
            $page = $PAGE;
        } else {
            $page = page_create_object($this->instance->pagetype, $this->instance->pageid);
        }
        $script = $page->url_get_full(array('instanceid' => $this->instance->id, 'sesskey' => $USER->sesskey));

        if (empty($this->instance->pinned)) {
            $movebuttons .= '<a class="icon roles" title="'. $this->str->assignroles .'" href="'.$CFG->wwwroot.'/'.$CFG->admin.'/roles/assign.php?contextid='.$context->id.'">' .
                            '<img src="'.$CFG->pixpath.'/i/roles.gif" alt="'.$this->str->assignroles.'" /></a>';
        }
     
        if ($this->user_can_edit()) {
            $movebuttons .= '<a class="icon hide" title="'. $title .'" href="'.$script.'&amp;blockaction=toggle">' .
                            '<img src="'. $CFG->pixpath.$icon .'" alt="'.$title.'" /></a>';
        }

        if ($options & BLOCK_CONFIGURE && $this->user_can_edit()) {
            $movebuttons .= '<a class="icon edit" title="'. $this->str->configure .'" href="'.$script.'&amp;blockaction=config">' .
                            '<img src="'. $CFG->pixpath .'/t/edit.gif" alt="'. $this->str->configure .'" /></a>';
        }

        if ($this->user_can_addto($page)) {
            $movebuttons .= '<a class="icon delete" title="'. $this->str->delete .'" href="'.$script.'&amp;blockaction=delete">' .
                            '<img src="'. $CFG->pixpath .'/t/delete.gif" alt="'. $this->str->delete .'" /></a>';
        }

        if ($options & BLOCK_MOVE_LEFT) {
            $movebuttons .= '<a class="icon left" title="'. $this->str->moveleft .'" href="'.$script.'&amp;blockaction=moveleft">' .
                            '<img src="'. $CFG->pixpath .'/t/'.$leftarrow.'" alt="'. $this->str->moveleft .'" /></a>';
        }
        if ($options & BLOCK_MOVE_UP) {
            $movebuttons .= '<a class="icon up" title="'. $this->str->moveup .'" href="'.$script.'&amp;blockaction=moveup">' .
                            '<img src="'. $CFG->pixpath .'/t/up.gif" alt="'. $this->str->moveup .'" /></a>';
        }
        if ($options & BLOCK_MOVE_DOWN) {
            $movebuttons .= '<a class="icon down" title="'. $this->str->movedown .'" href="'.$script.'&amp;blockaction=movedown">' .
                            '<img src="'. $CFG->pixpath .'/t/down.gif" alt="'. $this->str->movedown .'" /></a>';
        }
        if ($options & BLOCK_MOVE_RIGHT) {
            $movebuttons .= '<a class="icon right" title="'. $this->str->moveright .'" href="'.$script.'&amp;blockaction=moveright">' .
                            '<img src="'. $CFG->pixpath .'/t/'.$rightarrow.'" alt="'. $this->str->moveright .'" /></a>';
        }

        $movebuttons .= '</div>';
        $this->edit_controls = $movebuttons;
    }

    /**
     * Tests if this block has been implemented correctly.
     * Also, $errors isn't used right now
     *
     * @return boolean
     */
     
    function _self_test() {
        // Tests if this block has been implemented correctly.
        // Also, $errors isn't used right now
        $errors = array();

        $correct = true;
        if ($this->get_title() === NULL) {
            $errors[] = 'title_not_set';
            $correct = false;
        }
        if (!in_array($this->get_content_type(), array(BLOCK_TYPE_LIST, BLOCK_TYPE_TEXT))) {
            $errors[] = 'invalid_content_type';
            $correct = false;
        }
        //following selftest was not working when roles&capabilities were used from block 
/*        if ($this->get_content() === NULL) {
            $errors[] = 'content_not_set';
            $correct = false;
        }*/
        if ($this->get_version() === NULL) {
            $errors[] = 'version_not_set';
            $correct = false;
        }

        $formats = $this->applicable_formats();
        if (empty($formats) || array_sum($formats) === 0) {
            $errors[] = 'no_formats';
            $correct = false;
        }

        $width = $this->preferred_width();
        if (!is_int($width) || $width <= 0) {
            $errors[] = 'invalid_width';
            $correct = false;
        }
        return $correct;
    }

    /**
     * Subclasses should override this and return true if the
     * subclass block has a config_global.html file.
     *
     * @return boolean
     */
    function has_config() {
        return false;
    }

    /**
     * Default behavior: print the config_global.html file
     * You don't need to override this if you're satisfied with the above
     *
     * @uses $CFG
     * @return boolean
     */
    function config_print() {
        // Default behavior: print the config_global.html file
        // You don't need to override this if you're satisfied with the above
        if (!$this->has_config()) {
            return false;
        }
        global $CFG;
        print_simple_box_start('center', '', '', 5, 'blockconfigglobal');
        include($CFG->dirroot.'/blocks/'. $this->name() .'/config_global.html');
        print_simple_box_end();
        return true;
    }
    
    /**
     * Default behavior: save all variables as $CFG properties
     * You don't need to override this if you 're satisfied with the above
     *
     * @param array $data
     * @return boolean
     */
    function config_save($data) {
        foreach ($data as $name => $value) {
            set_config($name, $value);
        }
        return true;
    }
    
    /**
     * Default case: the block can be used in all course types
     * @return array
     * @todo finish documenting this function
     */
    function applicable_formats() {
        // Default case: the block can be used in courses and site index, but not in activities
        return array('all' => true, 'mod' => false, 'tag' => false);
    }
    

    /**
     * Default case: the block wants to be 180 pixels wide
     * @return int
     */
    function preferred_width() {
        return 180;
    }
    
    /**
     * Default return is false - header will be shown
     * @return boolean
     */
    function hide_header() {
        return false;
    }

    /**
     * Default case: an id with the instance and a class with our name in it
     * @return array
     * @todo finish documenting this function
     */
    function html_attributes() {
        return array('id' => 'inst'.$this->instance->id, 'class' => 'block_'. $this->name());
    }
    
    /**
     * Given an instance set the class var $instance to it and
     * load class var $config
     * @param block $instance
     * @todo add additional documentation to further explain the format of instance and config
     */
    function _load_instance($instance) {
        if (!empty($instance->configdata)) {
            $this->config = unserialize(base64_decode($instance->configdata));
        }
        // [pj] This line below is supposed to be an optimization (we don't need configdata anymore)
        // but what it does is break in PHP5 because the same instance object will be passed to
        // this function twice in each page view, and the second time it won't have any configdata
        // so it won't work correctly. Thus it's commented out.
        // unset($instance->configdata);
        $this->instance = $instance;
        $this->specialization();
    }

    /**
     * This function is called on your subclass right after an instance is loaded
     * Use this function to act on instance data just after it's loaded and before anything else is done
     * For instance: if your block will have different title's depending on location (site, course, blog, etc)
     */
    function specialization() {
        // Just to make sure that this method exists.
    }

    /**
     * Is each block of this type going to have instance-specific configuration?
     * Normally, this setting is controlled by {@link instance_allow_multiple}: if multiple
     * instances are allowed, then each will surely need its own configuration. However, in some
     * cases it may be necessary to provide instance configuration to blocks that do not want to
     * allow multiple instances. In that case, make this function return true.
     * I stress again that this makes a difference ONLY if {@link instance_allow_multiple} returns false.
     * @return boolean
     * @todo finish documenting this function by explaining per-instance configuration further
     */
    function instance_allow_config() {
        return false;
    }

    /**
     * Are you going to allow multiple instances of each block?
     * If yes, then it is assumed that the block WILL USE per-instance configuration
     * @return boolean
     * @todo finish documenting this function by explaining per-instance configuration further
     */
    function instance_allow_multiple() {
        // Are you going to allow multiple instances of each block?
        // If yes, then it is assumed that the block WILL USE per-instance configuration
        return false;
    }

    /**
     * Default behavior: print the config_instance.html file
     * You don't need to override this if you're satisfied with the above
     *
     * @uses $CFG
     * @return boolean
     * @todo finish documenting this function
     */
    function instance_config_print() {
        // Default behavior: print the config_instance.html file
        // You don't need to override this if you're satisfied with the above
        if (!$this->instance_allow_multiple() && !$this->instance_allow_config()) {
            return false;
        }
        global $CFG;

        if (is_file($CFG->dirroot .'/blocks/'. $this->name() .'/config_instance.html')) {
            print_simple_box_start('center', '', '', 5, 'blockconfiginstance');
            include($CFG->dirroot .'/blocks/'. $this->name() .'/config_instance.html');
            print_simple_box_end();
        } else {
            notice(get_string('blockconfigbad'), str_replace('blockaction=', 'dummy=', qualified_me()));
        }
        
        return true;
    }

    /**
     * Serialize and store config data
     * @return boolean
     * @todo finish documenting this function
     */
    function instance_config_save($data,$pinned=false) {
        $data = stripslashes_recursive($data);
        $this->config = $data;
        $table = 'block_instance';
        if (!empty($pinned)) {
            $table = 'block_pinned';
        }
        return set_field($table, 'configdata', base64_encode(serialize($data)), 'id', $this->instance->id);
    }

    /**
     * Replace the instance's configuration data with those currently in $this->config;
     * @return boolean
     * @todo finish documenting this function
     */
    function instance_config_commit($pinned=false) {
        $table = 'block_instance';
        if (!empty($pinned)) {
            $table = 'block_pinned';
        }
        return set_field($table, 'configdata', base64_encode(serialize($this->config)), 'id', $this->instance->id);
    }

     /**
     * Do any additional initialization you may need at the time a new block instance is created
     * @return boolean
     * @todo finish documenting this function
     */
    function instance_create() {
        return true;
    }

     /**
     * Delete everything related to this instance if you have been using persistent storage other than the configdata field.
     * @return boolean
     * @todo finish documenting this function
     */
    function instance_delete() {
        return true;
    }

     /**
     * Allows the block class to have a say in the user's ability to edit (i.e., configure) blocks of this type.
     * The framework has first say in whether this will be allowed (e.g., no editing allowed unless in edit mode)
     * but if the framework does allow it, the block can still decide to refuse.
     * @return boolean
     * @todo finish documenting this function
     */
    function user_can_edit() {
        return true;
    }

     /**
     * Allows the block class to have a say in the user's ability to create new instances of this block.
     * The framework has first say in whether this will be allowed (e.g., no adding allowed unless in edit mode)
     * but if the framework does allow it, the block can still decide to refuse.
     * This function has access to the complete page object, the creation related to which is being determined.
     * @return boolean
     * @todo finish documenting this function
     */
    function user_can_addto(&$page) {
        return true;
    }

    function get_extra_capabilities() {
        return array('moodle/block:view');
    }
}

/**
 * Specialized class for displaying a block with a list of icons/text labels
 *
 * @author Jon Papaioannou
 * @package blocks
 */

class block_list extends block_base {
    var $content_type  = BLOCK_TYPE_LIST;

    function is_empty() {

        if (empty($this->instance->pinned)) {
            $context = get_context_instance(CONTEXT_BLOCK, $this->instance->id);
        } else {
            $context = get_context_instance(CONTEXT_SYSTEM); // pinned blocks do not have own context
        }
        
        if ( !has_capability('moodle/block:view', $context) ) {
            return true;
        }

        $this->get_content();
        return (empty($this->content->items) && empty($this->content->footer));
    }

    function _print_block() {
        global $COURSE;

        // is_empty() includes a call to get_content()
        if ($this->is_empty() && empty($COURSE->javascriptportal)) {
            if (empty($this->edit_controls)) {
                // No content, no edit controls, so just shut up
                return;
            } else {
                // No content but editing, so show something at least
                $this->_print_shadow();
            }
        } else {
            if ($this->hide_header() && empty($this->edit_controls)) {
                // Header wants to hide, no edit controls to show, so no header it is
                print_side_block(NULL, '', $this->content->items, $this->content->icons, 
                                 $this->content->footer, $this->html_attributes());
            } else {
                // The full treatment, please. Include the title text.
                print_side_block($this->_title_html(), '', $this->content->items, $this->content->icons, 
                                 $this->content->footer, $this->html_attributes(), $this->title);
            }
        }
    }

}

?>
