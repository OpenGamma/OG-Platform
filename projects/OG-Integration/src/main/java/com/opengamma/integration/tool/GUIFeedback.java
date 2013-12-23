/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for offering visual feedback while a script runs. Most scripts will be scheduled tasks that will write details to the console or a log and not require this. Some are intended to be
 * launched from a desktop environment (for example a link on the Windows Start Menu) which require a more sophisticated feedback mechanism.
 */
public class GUIFeedback implements AutoCloseable {

  private static final Logger s_logger = LoggerFactory.getLogger(GUIFeedback.class);

  private static final boolean ENABLED = System.getProperty("tool.gui", "FALSE").equalsIgnoreCase("TRUE");

  private static final Object LOCK = new Object();

  private static GUIFeedback s_instance;

  private static final class Impl extends JDialog {

    private static final long serialVersionUID = 1L;

    private int _locked = 1;
    private JLabel _message;
    private JButton _button;
    private JProgressBar _progress;
    private int _totalWork;
    private int _completedWork;

    private JPanel margin() {
      final JPanel panel = new JPanel();
      panel.setPreferredSize(new Dimension(16, 16));
      return panel;
    }

    private JPanel messageControl(final String message) {
      _message = new JLabel(message);
      final JPanel panel = new JPanel() {

        private static final long serialVersionUID = 1L;

        private int _largestWidth;
        private int _largestHeight;

        @Override
        public Dimension getPreferredSize() {
          Dimension min = super.getPreferredSize();
          if (min.width > _largestWidth) {
            _largestWidth = min.width;
          } else if (min.width < _largestWidth) {
            min = new Dimension(_largestWidth, min.height);
          }
          if (min.height > _largestHeight) {
            _largestHeight = min.height;
          } else if (min.height < _largestHeight) {
            min = new Dimension(min.width, _largestHeight);
          }
          return min;
        }

      };
      final FlowLayout layout = new FlowLayout();
      layout.setAlignment(FlowLayout.CENTER);
      panel.setLayout(layout);
      panel.add(_message);
      return panel;
    }

    private JPanel messageAndProgressBar(final String message) {
      final JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());
      panel.add(messageControl(message), BorderLayout.CENTER);
      _progress = new JProgressBar();
      panel.add(_progress, BorderLayout.SOUTH);
      return panel;
    }

    private JPanel button() {
      _button = new JButton("Ok");
      final JPanel panel = new JPanel();
      final FlowLayout layout = new FlowLayout();
      layout.setAlignment(FlowLayout.CENTER);
      panel.setLayout(layout);
      panel.add(margin());
      panel.add(_button);
      panel.add(margin());
      _button.setVisible(false);
      return panel;
    }

    private void repack() {
      final int width = getWidth();
      final int height = getHeight();
      pack();
      if ((width != getWidth()) || (height != getHeight())) {
        final Point pos = getLocation();
        setLocation(pos.x + ((width - getWidth()) >> 1), pos.y + ((height - getHeight()) >> 1));
      }
    }

    private void showWindow(final String message) {
      s_logger.info("Showing window");
      setTitle("OpenGamma Platform");
      final Container inner = getContentPane();
      inner.setLayout(new BorderLayout());
      inner.add(margin(), BorderLayout.NORTH);
      inner.add(margin(), BorderLayout.EAST);
      inner.add(margin(), BorderLayout.WEST);
      inner.add(messageAndProgressBar(message), BorderLayout.CENTER);
      inner.add(button(), BorderLayout.SOUTH);
      pack();
      final Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
      setLocation((scr.width - getWidth()) >> 1, (scr.height - getHeight() >> 1));
      setVisible(true);
      addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(final WindowEvent e) {
          System.exit(2);
        }
      });
    }

    private void hideWindow() {
      s_logger.info("Hiding window");
      setVisible(false);
    }

    private void messageBox(final String message) {
      s_logger.info("Showing message box '{}'", message);
      final String previous = _message.getText();
      _message.setText(message);
      _button.setVisible(true);
      repack();
      final BlockingQueue<String> buttonActions = new LinkedBlockingQueue<String>();
      final ActionListener listener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          buttonActions.add(e.getActionCommand());
        }
      };
      _button.addActionListener(listener);
      try {
        buttonActions.take();
      } catch (InterruptedException ex) {
        s_logger.error("Interrupted", ex);
      }
      _button.removeActionListener(listener);
      _button.setVisible(false);
      _message.setText(previous);
      repack();
    }

    private void sayImpl(final String message) {
      s_logger.info("Displaying text '{}'", message);
      _message.setText(message);
      repack();
    }

    private void lock(final String message) {
      _locked++;
      sayImpl(message);
    }

    private void unlock(final String message, final int work) {
      updateProgressBar(0, work);
      if (--_locked == 0) {
        messageBox(message);
        hideWindow();
      } else {
        sayImpl(message);
      }
    }

    private void updateProgressBar(final int required, final int completed) {
      _totalWork += required;
      _progress.setMaximum(_totalWork);
      _completedWork += completed;
      _progress.setValue(_completedWork);
      s_logger.debug("Update progress bar - {} of {}", _completedWork, _totalWork);
    }

  }

  private final Impl _impl;
  private boolean _closed;

  public GUIFeedback(final String message) {
    s_logger.debug("Creating instance");
    s_logger.info("{}", message);
    if (ENABLED) {
      _impl = new Impl();
      synchronized (LOCK) {
        if (s_instance == null) {
          s_instance = this;
          _impl.showWindow(message);
        } else {
          s_instance._impl.lock(message);
        }
      }
    } else {
      _impl = null;
    }
  }

  /**
   * Reports an informational message to the user about what is going on. Ideally, a new message should appear at least every 15 seconds (unless the progress bar is used) but no more often than every
   * two seconds.
   * 
   * @param message the message to write, not null
   */
  public static void say(final String message) {
    s_logger.info("{}", message);
    if (ENABLED) {
      synchronized (LOCK) {
        if (s_instance != null) {
          s_instance._impl.sayImpl(message);
        }
      }
    }
  }

  /**
   * Reports a message to the user which will require acknowledgement. This should only happen as part of task completion (see {@link #done}) or in the case of an error.
   * 
   * @param message the message to write, not null
   */
  public static void shout(final String message) {
    s_logger.error("{}", message);
    if (ENABLED) {
      synchronized (LOCK) {
        if (s_instance != null) {
          s_instance._impl.messageBox(message);
        }
      }
    }
  }

  /**
   * Reports an increase in the amount of work required.
   * 
   * @param count an arbitrary number of work units
   */
  public void workRequired(final int count) {
    if (ENABLED) {
      if (!_closed) {
        _impl._totalWork += count;
        synchronized (LOCK) {
          if (s_instance != null) {
            s_instance._impl.updateProgressBar(count, 0);
          }
        }
      }
    }
  }

  /**
   * Reports completion of an amount of work.
   * 
   * @param count the amount of arbitrary work units completed
   */
  public void workCompleted(int count) {
    if (ENABLED) {
      if (!_closed) {
        _impl._completedWork += count;
        if (_impl._completedWork > _impl._totalWork) {
          count -= _impl._completedWork - _impl._totalWork;
          _impl._completedWork = _impl._totalWork;
        }
        synchronized (LOCK) {
          if (s_instance != null) {
            s_instance._impl.updateProgressBar(0, count);
          }
        }
      }
    }
  }

  /**
   * Reports completion of this feedback instance. This is the equivalent of calling {@link #close} but will display a message at the same time. If this is the primary instance the message will
   * require acknowledgement. If this is a nested instance, the message is just displayed and this method returns.
   * 
   * @param message the message to display, not null
   */
  public void done(final String message) {
    s_logger.info("{}", message);
    if (ENABLED) {
      if (!_closed) {
        _closed = true;
        synchronized (LOCK) {
          s_instance._impl.unlock(message, _impl._totalWork - _impl._completedWork);
        }
      }
    }
  }

  // Autocloseable

  @Override
  public void close() throws Exception {
    if (ENABLED) {
      if (!_closed) {
        _closed = true;
        synchronized (LOCK) {
          s_instance._impl.unlock("Finished", _impl._totalWork - _impl._completedWork);
        }
      }
    }
    s_logger.debug("Destroyed feedback instance");
  }

}
