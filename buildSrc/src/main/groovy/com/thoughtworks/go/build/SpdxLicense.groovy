/*
 * Copyright Thoughtworks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.go.build

enum SpdxLicense {
  GLIDE("Glide", "3dfx Glide License"),
  ABSTYLES("Abstyles", "Abstyles License"),
  AFL_1_1("AFL-1.1", "Academic Free License v1.1"),
  AFL_1_2("AFL-1.2", "Academic Free License v1.2"),
  AFL_2_0("AFL-2.0", "Academic Free License v2.0"),
  AFL_2_1("AFL-2.1", "Academic Free License v2.1"),
  AFL_3_0("AFL-3.0", "Academic Free License v3.0"),
  AMPAS("AMPAS", "Academy of Motion Picture Arts and Sciences BSD"),
  APL_1_0("APL-1.0", "Adaptive Public License 1.0"),
  ADOBE_GLYPH("Adobe-Glyph", "Adobe Glyph List License"),
  APAFML("APAFML", "Adobe Postscript AFM License"),
  ADOBE_2006("Adobe-2006", "Adobe Systems Incorporated Source Code License Agreement"),
  AGPL_1_0("AGPL-1.0", "Affero General Public License v1.0"),
  AFMPARSE("Afmparse", "Afmparse License"),
  ALADDIN("Aladdin", "Aladdin Free Public License"),
  ADSL("ADSL", "Amazon Digital Services License"),
  AMDPLPA("AMDPLPA", "AMD's plpa_map.c License"),
  ANTLR_PD("ANTLR-PD", "ANTLR Software Rights Notice"),
  APACHE_1_0("Apache-1.0", "Apache License 1.0"),
  APACHE_1_1("Apache-1.1", "Apache License 1.1", "Apache Software License, Version 1.1", "Similar to Apache License but with the acknowledgment clause removed", "The OpenSymphony Software License 1.1"),
  APACHE_2_0("Apache-2.0", "Apache License 2.0", "Apache License, Version 2.0"),
  AML("AML", "Apple MIT License"),
  APSL_1_0("APSL-1.0", "Apple Public Source License 1.0"),
  APSL_1_1("APSL-1.1", "Apple Public Source License 1.1"),
  APSL_1_2("APSL-1.2", "Apple Public Source License 1.2"),
  APSL_2_0("APSL-2.0", "Apple Public Source License 2.0"),
  ARTISTIC_1_0("Artistic-1.0", "Artistic License 1.0"),
  ARTISTIC_1_0_PERL("Artistic-1.0-Perl", "Artistic License 1.0 (Perl)"),
  ARTISTIC_1_0_CL8("Artistic-1.0-cl8", "Artistic License 1.0 w/clause 8"),
  ARTISTIC_2_0("Artistic-2.0", "Artistic License 2.0"),
  AAL("AAL", "Attribution Assurance License"),
  BAHYPH("Bahyph", "Bahyph License"),
  BARR("Barr", "Barr License"),
  BEERWARE("Beerware", "Beerware License"),
  BITTORRENT_1_0("BitTorrent-1.0", "BitTorrent Open Source License v1.0"),
  BITTORRENT_1_1("BitTorrent-1.1", "BitTorrent Open Source License v1.1"),
  BSL_1_0("BSL-1.0", "Boost Software License 1.0"),
  BORCEUX("Borceux", "Borceux license"),
  BSD_2_CLAUSE("BSD-2-Clause", "BSD 2-clause \"Simplified\" License", "The 2-Clause BSD License"),
  BSD_2_CLAUSE_FREEBSD("BSD-2-Clause-FreeBSD", "BSD 2-clause FreeBSD License"),
  BSD_2_CLAUSE_NETBSD("BSD-2-Clause-NetBSD", "BSD 2-clause NetBSD License"),
  BSD_3_CLAUSE("BSD-3-Clause", "BSD 3-clause \"New\" or \"Revised\" License", "BSD 3-Clause", "The BSD 3-Clause License", "The 3-Clause BSD License", "BSD License 2.0"),
  BSD_3_CLAUSE_CLEAR("BSD-3-Clause-Clear", "BSD 3-clause Clear License"),
  BSD_4_ClAUSE("BSD-4-Clause", "BSD 4-clause \"Original\" or \"Old\" License"),
  BSD_PROTECTION("BSD-Protection", "BSD Protection License"),
  BSD_3_CLAUSE_ATTRIBUTION("BSD-3-Clause-Attribution", "BSD with attribution"),
  BSD_0("0BSD", "BSD Zero Clause License"),
  BSD_4_CLAUSE_UC("BSD-4-Clause-UC", "BSD-4-Clause (University of California-Specific)"),
  BZIP2_1_0_5("bzip2-1.0.5", "bzip2 and libbzip2 License v1.0.5"),
  BZIP2_1_0_6("bzip2-1.0.6", "bzip2 and libbzip2 License v1.0.6"),
  CALDERA("Caldera", "Caldera License"),
  CECILL_1_0("CECILL-1.0", "CeCILL Free Software License Agreement v1.0"),
  CECILL_1_1("CECILL-1.1", "CeCILL Free Software License Agreement v1.1"),
  CECILL_2_0("CECILL-2.0", "CeCILL Free Software License Agreement v2.0"),
  CECILL_2_1("CECILL-2.1", "CeCILL Free Software License Agreement v2.1"),
  CECILL_B("CECILL-B", "CeCILL-B Free Software License Agreement"),
  CECILL_C("CECILL-C", "CeCILL-C Free Software License Agreement"),
  CLARTISTIC("ClArtistic", "Clarified Artistic License"),
  MIT_CMU("MIT-CMU", "CMU License"),
  CNRI_JYTHON("CNRI-Jython", "CNRI Jython License"),
  CNRI_PYTHON("CNRI-Python", "CNRI Python License"),
  CNRI_PYTHON_GPL_COMPATIBLE("CNRI-Python-GPL-Compatible", "CNRI Python Open Source GPL Compatible License Agreement"),
  CPOL_1_02("CPOL-1.02", "Code Project Open License 1.02"),
  CDDL_1_0("CDDL-1.0", "Common Development and Distribution License 1.0", "Common Development and Distribution License (CDDL), Version 1.0", "Common Development and Distribution License (CDDL) v1.0", "Common Development and Distribution License (CDDL) Version 1.0"),
  CDDL_1_1("CDDL-1.1", "Common Development and Distribution License 1.1", "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.1"),
  CPAL_1_0("CPAL-1.0", "Common Public Attribution License 1.0"),
  CPL_1_0("CPL-1.0", "Common Public License 1.0"),
  CATOSL_1_1("CATOSL-1.1", "Computer Associates Trusted Open Source License 1.1"),
  CONDOR_1_1("Condor-1.1", "Condor Public License v1.1"),
  CC_BY_1_0("CC-BY-1.0", "Creative Commons Attribution 1.0"),
  CC_BY_2_0("CC-BY-2.0", "Creative Commons Attribution 2.0"),
  CC_BY_2_5("CC-BY-2.5", "Creative Commons Attribution 2.5"),
  CC_BY_3_0("CC-BY-3.0", "Creative Commons Attribution 3.0"),
  CC_BY_4_0("CC-BY-4.0", "Creative Commons Attribution 4.0"),
  CC_BY_ND_1_0("CC-BY-ND-1.0", "Creative Commons Attribution No Derivatives 1.0"),
  CC_BY_ND_2_0("CC-BY-ND-2.0", "Creative Commons Attribution No Derivatives 2.0"),
  CC_BY_ND_2_5("CC-BY-ND-2.5", "Creative Commons Attribution No Derivatives 2.5"),
  CC_BY_ND_3_0("CC-BY-ND-3.0", "Creative Commons Attribution No Derivatives 3.0"),
  CC_BY_ND_4_0("CC-BY-ND-4.0", "Creative Commons Attribution No Derivatives 4.0"),
  CC_BY_NC_1_0("CC-BY-NC-1.0", "Creative Commons Attribution Non Commercial 1.0"),
  CC_BY_NC_2_0("CC-BY-NC-2.0", "Creative Commons Attribution Non Commercial 2.0"),
  CC_BY_NC_2_5("CC-BY-NC-2.5", "Creative Commons Attribution Non Commercial 2.5"),
  CC_BY_NC_3_0("CC-BY-NC-3.0", "Creative Commons Attribution Non Commercial 3.0"),
  CC_BY_NC_4_0("CC-BY-NC-4.0", "Creative Commons Attribution Non Commercial 4.0"),
  CC_BY_NC_ND_1_0("CC-BY-NC-ND-1.0", "Creative Commons Attribution Non Commercial No Derivatives 1.0"),
  CC_BY_NC_ND_2_0("CC-BY-NC-ND-2.0", "Creative Commons Attribution Non Commercial No Derivatives 2.0"),
  CC_BY_NC_ND_2_5("CC-BY-NC-ND-2.5", "Creative Commons Attribution Non Commercial No Derivatives 2.5"),
  CC_BY_NC_ND_3_0("CC-BY-NC-ND-3.0", "Creative Commons Attribution Non Commercial No Derivatives 3.0"),
  CC_BY_NC_ND_4_0("CC-BY-NC-ND-4.0", "Creative Commons Attribution Non Commercial No Derivatives 4.0"),
  CC_BY_NC_SA_1_0("CC-BY-NC-SA-1.0", "Creative Commons Attribution Non Commercial Share Alike 1.0"),
  CC_BY_NC_SA_2_0("CC-BY-NC-SA-2.0", "Creative Commons Attribution Non Commercial Share Alike 2.0"),
  CC_BY_NC_SA_2_5("CC-BY-NC-SA-2.5", "Creative Commons Attribution Non Commercial Share Alike 2.5"),
  CC_BY_NC_SA_3_0("CC-BY-NC-SA-3.0", "Creative Commons Attribution Non Commercial Share Alike 3.0"),
  CC_BY_NC_SA_4_0("CC-BY-NC-SA-4.0", "Creative Commons Attribution Non Commercial Share Alike 4.0"),
  CC_BY_SA_1_0("CC-BY-SA-1.0", "Creative Commons Attribution Share Alike 1.0"),
  CC_BY_SA_2_0("CC-BY-SA-2.0", "Creative Commons Attribution Share Alike 2.0"),
  CC_BY_SA_2_5("CC-BY-SA-2.5", "Creative Commons Attribution Share Alike 2.5"),
  CC_BY_SA_3_0("CC-BY-SA-3.0", "Creative Commons Attribution Share Alike 3.0"),
  CC_BY_SA_4_0("CC-BY-SA-4.0", "Creative Commons Attribution Share Alike 4.0"),
  CC0_1_0("CC0-1.0", "Creative Commons Zero v1.0 Universal"),
  CROSSWORD("Crossword", "Crossword License"),
  CRYSTALSTACKER("CrystalStacker", "CrystalStacker License"),
  CUA_OPL_1_0("CUA-OPL-1.0", "CUA Office Public License v1.0"),
  CUBE("Cube", "Cube License"),
  CURL("curl", "curl License"),
  D_FSL_1_0("D-FSL-1.0", "Deutsche Freie Software Lizenz"),
  DIFFMARK("diffmark", "diffmark license"),
  WTFPL("WTFPL", "Do What The F*ck You Want To Public License"),
  DOC("DOC", "DOC License"),
  DOTSEQN("Dotseqn", "Dotseqn License"),
  DSDP("DSDP", "DSDP License"),
  DVIPDFM("dvipdfm", "dvipdfm License"),
  EPL_1_0("EPL-1.0", "Eclipse Public License 1.0", "Eclipse Public License - v 1.0", "Eclipse Public License - Version 1.0"),
  EPL_2_0("EPL-2.0", "Eclipse Public License 2.0", "Eclipse Public License - v 2.0", "Eclipse Public License - Version 2.0"),
  ECL_1_0("ECL-1.0", "Educational Community License v1.0"),
  ECL_2_0("ECL-2.0", "Educational Community License v2.0"),
  EGENIX("eGenix", "eGenix.com Public License 1.1.0"),
  EFL_1_0("EFL-1.0", "Eiffel Forum License v1.0"),
  EFL_2_0("EFL-2.0", "Eiffel Forum License v2.0"),
  MIT_ADVERTISING("MIT-advertising", "Enlightenment License (e16)"),
  MIT_ENNA("MIT-enna", "enna License"),
  ENTESSA("Entessa", "Entessa Public License v1.0"),
  ERLPL_1_1("ErlPL-1.1", "Erlang Public License v1.1"),
  EUDATAGRID("EUDatagrid", "EU DataGrid Software License"),
  EUPL_1_0("EUPL-1.0", "European Union Public License 1.0"),
  EUPL_1_1("EUPL-1.1", "European Union Public License 1.1"),
  EUROSYM("Eurosym", "Eurosym License"),
  FAIR("Fair", "Fair License"),
  MIT_FEH("MIT-feh", "feh License"),
  FRAMEWORX_1_0("Frameworx-1.0", "Frameworx Open License 1.0"),
  FREEIMAGE("FreeImage", "FreeImage Public License v1.0"),
  FTL("FTL", "Freetype Project License"),
  FSFAP("FSFAP", "FSF All Permissive License"),
  FSFUL("FSFUL", "FSF Unlimited License"),
  FSFULLR("FSFULLR", "FSF Unlimited License (with License Retention)"),
  GIFTWARE("Giftware", "Giftware License"),
  GL2PS("GL2PS", "GL2PS License"),
  GLULXE("Glulxe", "Glulxe License"),
  AGPL_3_0("AGPL-3.0", "GNU Affero General Public License v3.0"),
  GFDL_1_1("GFDL-1.1", "GNU Free Documentation License v1.1"),
  GFDL_1_2("GFDL-1.2", "GNU Free Documentation License v1.2"),
  GFDL_1_3("GFDL-1.3", "GNU Free Documentation License v1.3"),
  GPL_1_0("GPL-1.0", "GNU General Public License v1.0 only"),
  GPL_2_0("GPL-2.0", "GNU General Public License v2.0 only"),
  GPL_3_0("GPL-3.0", "GNU General Public License v3.0 only"),
  LGPL_2_1("LGPL-2.1", "GNU Lesser General Public License v2.1 only", "LGPL 2.1", 'GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1'),
  LGPL_3_0_ONLY("LGPL-3.0-only", "GNU Lesser General Public License v3.0 only", "GNU Lesser General Public License"),
  LGPL_3_0("LGPL-3.0", "GNU Lesser General Public License v3.0", "GNU LESSER GENERAL PUBLIC LICENSE, Version 3"),
  LGPL_2_0("LGPL-2.0", "GNU Library General Public License v2 only"),
  GNUPLOT("gnuplot", "gnuplot License"),
  GSOAP_1_3B("gSOAP-1.3b", "gSOAP Public License v1.3b"),
  HASKELLREPORT("HaskellReport", "Haskell Language Report License"),
  HPND("HPND", "Historic Permission Notice and Disclaimer"),
  IBM_PIBS("IBM-pibs", "IBM PowerPC Initialization and Boot Software"),
  IPL_1_0("IPL-1.0", "IBM Public License v1.0"),
  ICU("ICU", "ICU License"),
  IMAGEMAGICK("ImageMagick", "ImageMagick License"),
  IMATIX("iMatix", "iMatix Standard Function Library Agreement"),
  IMLIB2("Imlib2", "Imlib2 License"),
  IJG("IJG", "Independent JPEG Group License"),
  INFO_ZIP("Info-ZIP", "Info-ZIP License"),
  INTEL_ACPI("Intel-ACPI", "Intel ACPI Software License Agreement"),
  INTEL("Intel", "Intel Open Source License"),
  INTERBASE_1_0("Interbase-1.0", "Interbase Public License v1.0"),
  IPA("IPA", "IPA Font License"),
  ISC("ISC", "ISC License"),
  JASPER_2_0("JasPer-2.0", "JasPer License"),
  JSON("JSON", "JSON License"),
  LPPL_1_0("LPPL-1.0", "LaTeX Project Public License v1.0"),
  LPPL_1_1("LPPL-1.1", "LaTeX Project Public License v1.1"),
  LPPL_1_2("LPPL-1.2", "LaTeX Project Public License v1.2"),
  LPPL_1_3A("LPPL-1.3a", "LaTeX Project Public License v1.3a"),
  LPPL_1_3C("LPPL-1.3c", "LaTeX Project Public License v1.3c"),
  LATEX2E("Latex2e", "Latex2e License"),
  BSD_3_CLAUSE_LBNL("BSD-3-Clause-LBNL", "Lawrence Berkeley National Labs BSD variant license"),
  LEPTONICA("Leptonica", "Leptonica License"),
  LGPLLR("LGPLLR", "Lesser General Public License For Linguistic Resources"),
  LIBPNG("Libpng", "libpng License"),
  LIBTIFF("libtiff", "libtiff License"),
  LAL_1_2("LAL-1.2", "Licence Art Libre 1.2"),
  LAL_1_3("LAL-1.3", "Licence Art Libre 1.3"),
  LILIQ_P_1_1("LiLiQ-P-1.1", "Licence Libre du Québec – Permissive version 1.1"),
  LILIQ_RPLUS_1_1("LiLiQ-Rplus-1.1", "Licence Libre du Québec – Réciprocité forte version 1.1"),
  LILIQ_R_1_1("LiLiQ-R-1.1", "Licence Libre du Québec – Réciprocité version 1.1"),
  LPL_1_02("LPL-1.02", "Lucent Public License v1.02"),
  LPL_1_0("LPL-1.0", "Lucent Public License Version 1.0"),
  MAKEINDEX("MakeIndex", "MakeIndex License"),
  MTLL("MTLL", "Matrix Template Library License"),
  MS_PL("MS-PL", "Microsoft Public License"),
  MS_RL("MS-RL", "Microsoft Reciprocal License"),
  MIROS("MirOS", "MirOS Licence"),
  MITNFA("MITNFA", "MIT +no-false-attribs license"),
  MIT("MIT", "MIT License", "The MIT License (MIT)", "Bouncy Castle Licence", "MIT*", "(MIT OR GPL-2.0)", "MIT AND (BSD-2-Clause OR GPL-2.0-or-later)"),
  MOTOSOTO("Motosoto", "Motosoto License"),
  MPL_1_0("MPL-1.0", "Mozilla Public License 1.0", "MPL 1.0"),
  MPL_1_1("MPL-1.1", "Mozilla Public License 1.1", "MPL 1.1"),
  MPL_2_0("MPL-2.0", "Mozilla Public License 2.0", "MPL 2.0"),
  MPL_2_0_NO_COPYLEFT_EXCEPTION("MPL-2.0-no-copyleft-exception", "Mozilla Public License 2.0 (no copyleft exception)"),
  MPICH2("mpich2", "mpich2 License"),
  MULTICS("Multics", "Multics License"),
  MUP("Mup", "Mup License"),
  NASA_1_3("NASA-1.3", "NASA Open Source Agreement 1.3"),
  NAUMEN("Naumen", "Naumen Public License"),
  NBPL_1_0("NBPL-1.0", "Net Boolean Public License v1"),
  NETCDF("NetCDF", "NetCDF license"),
  NGPL("NGPL", "Nethack General Public License"),
  NOSL("NOSL", "Netizen Open Source License"),
  NPL_1_0("NPL-1.0", "Netscape Public License v1.0"),
  NPL_1_1("NPL-1.1", "Netscape Public License v1.1"),
  NEWSLETR("Newsletr", "Newsletr License"),
  NLPL("NLPL", "No Limit Public License"),
  NOKIA("Nokia", "Nokia Open Source License"),
  NPOSL_3_0("NPOSL-3.0", "Non-Profit Open Software License 3.0"),
  NLOD_1_0("NLOD-1.0", "Norwegian Licence for Open Government Data"),
  NOWEB("Noweb", "Noweb License"),
  NRL("NRL", "NRL License"),
  NTP("NTP", "NTP License"),
  NUNIT("Nunit", "Nunit License"),
  OCLC_2_0("OCLC-2.0", "OCLC Research Public License 2.0"),
  ODBL_1_0("ODbL-1.0", "ODC Open Database License v1.0"),
  PDDL_1_0("PDDL-1.0", "ODC Public Domain Dedication & License 1.0"),
  OCCT_PL("OCCT-PL", "Open CASCADE Technology Public License"),
  OGTSL("OGTSL", "Open Group Test Suite License"),
  OLDAP_2_2_2("OLDAP-2.2.2", "Open LDAP Public License 2.2.2"),
  OLDAP_1_1("OLDAP-1.1", "Open LDAP Public License v1.1"),
  OLDAP_1_2("OLDAP-1.2", "Open LDAP Public License v1.2"),
  OLDAP_1_3("OLDAP-1.3", "Open LDAP Public License v1.3"),
  OLDAP_1_4("OLDAP-1.4", "Open LDAP Public License v1.4"),
  OLDAP_2_0("OLDAP-2.0", "Open LDAP Public License v2.0 (or possibly 2.0A and 2.0B)"),
  OLDAP_2_0_1("OLDAP-2.0.1", "Open LDAP Public License v2.0.1"),
  OLDAP_2_1("OLDAP-2.1", "Open LDAP Public License v2.1"),
  OLDAP_2_2("OLDAP-2.2", "Open LDAP Public License v2.2"),
  OLDAP_2_2_1("OLDAP-2.2.1", "Open LDAP Public License v2.2.1"),
  OLDAP_2_3("OLDAP-2.3", "Open LDAP Public License v2.3"),
  OLDAP_2_4("OLDAP-2.4", "Open LDAP Public License v2.4"),
  OLDAP_2_5("OLDAP-2.5", "Open LDAP Public License v2.5"),
  OLDAP_2_6("OLDAP-2.6", "Open LDAP Public License v2.6"),
  OLDAP_2_7("OLDAP-2.7", "Open LDAP Public License v2.7"),
  OLDAP_2_8("OLDAP-2.8", "Open LDAP Public License v2.8"),
  OML("OML", "Open Market License"),
  OPL_1_0("OPL-1.0", "Open Public License v1.0"),
  OSL_1_0("OSL-1.0", "Open Software License 1.0"),
  OSL_1_1("OSL-1.1", "Open Software License 1.1"),
  OSL_2_0("OSL-2.0", "Open Software License 2.0"),
  OSL_2_1("OSL-2.1", "Open Software License 2.1"),
  OSL_3_0("OSL-3.0", "Open Software License 3.0"),
  OPENSSL("OpenSSL", "OpenSSL License"),
  OSET_PL_2_1("OSET-PL-2.1", "OSET Public License version 2.1"),
  PHP_3_0("PHP-3.0", "PHP License v3.0"),
  PHP_3_01("PHP-3.01", "PHP License v3.01"),
  PLEXUS("Plexus", "Plexus Classworlds License"),
  POSTGRESQL("PostgreSQL", "PostgreSQL License"),
  PSFRAG("psfrag", "psfrag License"),
  PSUTILS("psutils", "psutils License"),
  PYTHON_2_0("Python-2.0", "Python License 2.0"),
  QPL_1_0("QPL-1.0", "Q Public License 1.0"),
  QHULL("Qhull", "Qhull License"),
  RDISC("Rdisc", "Rdisc License"),
  RPSL_1_0("RPSL-1.0", "RealNetworks Public Source License v1.0"),
  RPL_1_1("RPL-1.1", "Reciprocal Public License 1.1"),
  RPL_1_5("RPL-1.5", "Reciprocal Public License 1.5"),
  RHECOS_1_1("RHeCos-1.1", "Red Hat eCos Public License v1.1"),
  RSCPL("RSCPL", "Ricoh Source Code Public License"),
  RSA_MD("RSA-MD", "RSA Message-Digest License"),
  RUBY("Ruby", "Ruby License"),
  SAX_PD("SAX-PD", "Sax Public Domain Notice"),
  SAXPATH("Saxpath", "Saxpath License"),
  SCEA("SCEA", "SCEA Shared Source License"),
  SWL("SWL", "Scheme Widget Library (SWL) Software License Agreement"),
  SMPPL("SMPPL", "Secure Messaging Protocol Public License"),
  SENDMAIL("Sendmail", "Sendmail License"),
  SGI_B_1_0("SGI-B-1.0", "SGI Free Software License B v1.0"),
  SGI_B_1_1("SGI-B-1.1", "SGI Free Software License B v1.1"),
  SGI_B_2_0("SGI-B-2.0", "SGI Free Software License B v2.0"),
  OFL_1_0("OFL-1.0", "SIL Open Font License 1.0"),
  OFL_1_1("OFL-1.1", "SIL Open Font License 1.1", "(CC-BY-4.0 AND OFL-1.1 AND MIT)"),
  SIMPL_2_0("SimPL-2.0", "Simple Public License 2.0"),
  SLEEPYCAT("Sleepycat", "Sleepycat License"),
  SNIA("SNIA", "SNIA Public License 1.1"),
  SPENCER_86("Spencer-86", "Spencer License 86"),
  SPENCER_94("Spencer-94", "Spencer License 94"),
  SPENCER_99("Spencer-99", "Spencer License 99"),
  SMLNJ("SMLNJ", "Standard ML of New Jersey License"),
  SUGARCRM_1_1_3("SugarCRM-1.1.3", "SugarCRM Public License v1.1.3"),
  SISSL("SISSL", "Sun Industry Standards Source License v1.1"),
  SISSL_1_2("SISSL-1.2", "Sun Industry Standards Source License v1.2"),
  SPL_1_0("SPL-1.0", "Sun Public License v1.0"),
  WATCOM_1_0("Watcom-1.0", "Sybase Open Watcom Public License 1.0"),
  TCL("TCL", "TCL/TK License"),
  UNLICENSE("Unlicense", "The Unlicense"),
  TMATE("TMate", "TMate Open Source License"),
  TORQUE_1_1("TORQUE-1.1", "TORQUE v2.5+ Software License v1.1"),
  TOSL("TOSL", "Trusster Open Source License"),
  UNICODE_TOU("Unicode-TOU", "Unicode Terms of Use"),
  UPL_1_0("UPL-1.0", "Universal Permissive License v1.0"),
  NCSA("NCSA", "University of Illinois/NCSA Open Source License"),
  VIM("Vim", "Vim License"),
  VOSTROM("VOSTROM", "VOSTROM Public License for Open Source"),
  VSL_1_0("VSL-1.0", "Vovida Software License v1.0"),
  W3C_19980720("W3C-19980720", "W3C Software Notice and License (1998-07-20)"),
  W3C("W3C", "W3C Software Notice and License (2002-12-31)"),
  WSUIPA("Wsuipa", "Wsuipa License"),
  XNET("Xnet", "X.Net License"),
  X11("X11", "X11 License"),
  XEROX("Xerox", "Xerox License"),
  XFREE86_1_1("XFree86-1.1", "XFree86 License 1.1"),
  XINETD("xinetd", "xinetd License"),
  XPP("xpp", "XPP License"),
  XSKAT("XSkat", "XSkat License"),
  YPL_1_0("YPL-1.0", "Yahoo! Public License v1.0"),
  YPL_1_1("YPL-1.1", "Yahoo! Public License v1.1"),
  ZED("Zed", "Zed License"),
  ZEND_2_0("Zend-2.0", "Zend License v2.0"),
  ZIMBRA_1_3("Zimbra-1.3", "Zimbra Public License v1.3"),
  ZIMBRA_1_4("Zimbra-1.4", "Zimbra Public License v1.4"),
  ZLIB("Zlib", "zlib License"),
  ZLIB_ACKNOWLEDGEMENT("zlib-acknowledgement", "zlib/libpng License with Acknowledgement"),
  ZPL_1_1("ZPL-1.1", "Zope Public License 1.1"),
  ZPL_2_0("ZPL-2.0", "Zope Public License 2.0"),
  ZPL_2_1("ZPL-2.1", "Zope Public License 2.1")

  public final Set<String> names
  public final String id

  SpdxLicense(String id, String... names) {
    this.names = names.collect { it -> it.toLowerCase() }
    this.id = id
  }

  static String normalizedLicense(String license) {
    if (license == null || license.allWhitespace) {
      return null
    }

    final String normalized = license.toLowerCase().trim().replaceAll(~/\s+/, ' ')
    for (SpdxLicense eachLicense : values()) {
      if (license == eachLicense.id || eachLicense.names.contains(normalized)) {
        return eachLicense.id
      }
    }

    return null
  }

}
